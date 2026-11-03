package com.d4rksoul.base.ecommerce.spring.react.service.admin;

import com.d4rksoul.base.ecommerce.spring.react.dto.request.admin.AdminOrderSearchRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.OrderDetailsResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.OrderShipmentResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.PageResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminOrderDetailsResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminOrderSummaryResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.Order;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderShipment;
import com.d4rksoul.base.ecommerce.spring.react.enums.OrderStatus;
import com.d4rksoul.base.ecommerce.spring.react.enums.ShipmentStatus;
import com.d4rksoul.base.ecommerce.spring.react.exception.OrderNotFoundException;
import com.d4rksoul.base.ecommerce.spring.react.exception.admin.InvalidOrderTransitionException;
import com.d4rksoul.base.ecommerce.spring.react.mapper.PageMapper;
import com.d4rksoul.base.ecommerce.spring.react.mapper.admin.AdminOrderMapper;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderShipmentRepository;
import com.d4rksoul.base.ecommerce.spring.react.service.OrderFulfillmentService;
import com.d4rksoul.base.ecommerce.spring.react.service.OrderService;
import com.d4rksoul.base.ecommerce.spring.react.specification.OrderSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminOrderService {

    private static final Map<ShipmentStatus, List<ShipmentStatus>> SHIPMENT_TRANSITIONS = Map.of(
            ShipmentStatus.PRE_TRANSIT, List.of(ShipmentStatus.IN_TRANSIT),
            ShipmentStatus.IN_TRANSIT, List.of(ShipmentStatus.OUT_FOR_DELIVERY),
            ShipmentStatus.OUT_FOR_DELIVERY, List.of(ShipmentStatus.FAILURE, ShipmentStatus.DELIVERED),
            ShipmentStatus.FAILURE, List.of(ShipmentStatus.OUT_FOR_DELIVERY, ShipmentStatus.DELIVERED)
    );

    private final OrderRepository orderRepository;
    private final OrderShipmentRepository orderShipmentRepository;
    private final OrderService orderService;
    private final OrderFulfillmentService orderFulfillmentService;
    private final AdminOrderMapper adminOrderMapper;
    private final PageMapper pageMapper;

    @Transactional(readOnly = true)
    public PageResponse<AdminOrderSummaryResponse> getOrders(AdminOrderSearchRequest filter) {
        Specification<Order> specification = Specification
                .where(OrderSpecification.hasStatusIn(
                        filter.pending() ? OrderStatus.FULFILLMENT_STATUSES : OrderStatus.HISTORY_STATUSES
                ))
                .and(OrderSpecification.hasStatus(filter.status()))
                .and(OrderSpecification.createdFrom(filter.from()))
                .and(OrderSpecification.createdTo(filter.to()))
                .and(OrderSpecification.matchesCustomerSearch(filter.q()))
                .and(filter.pending()
                        ? OrderSpecification.orderByFulfillmentPriority()
                        : OrderSpecification.orderByRecentDesc()
                );

        Pageable pageable = PageRequest.of(filter.page(), filter.size());
        Page<AdminOrderSummaryResponse> ordersPage = orderRepository.findAll(specification, pageable)
                .map(adminOrderMapper::toSummaryDto);

        return pageMapper.toDto(ordersPage);
    }

    @Transactional(readOnly = true)
    public AdminOrderDetailsResponse getOrder(Long id) {
        Order order = orderRepository.findByIdAndStatusIn(id, OrderStatus.HISTORY_STATUSES)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));

        OrderDetailsResponse details = orderService.buildDetails(order);
        OrderStatus nextStatus = order.getStatus() == OrderStatus.PAID ? OrderStatus.PROCESSING : null;
        Map<Long, List<ShipmentStatus>> shipmentActions = details.shipments().stream()
                .collect(Collectors.toMap(
                        OrderShipmentResponse::id,
                        shipment -> SHIPMENT_TRANSITIONS.getOrDefault(
                                shipment.status(),
                                List.of()
                        )
                ));

        return adminOrderMapper.toDetailsDto(order, details, nextStatus, shipmentActions);
    }

    @Transactional
    public void changeStatus(Long id, OrderStatus target) {
        Order order = orderRepository.findByIdAndStatusIn(id, OrderStatus.HISTORY_STATUSES)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));

        if (order.getStatus() != OrderStatus.PAID || target != OrderStatus.PROCESSING) {
            throw new InvalidOrderTransitionException(
                    "Order " + id + " cannot move from " + order.getStatus() + " to " + target + "."
            );
        }

        if (!orderFulfillmentService.startProcessing(id)) {
            throw new InvalidOrderTransitionException(
                    "Order " + id + " was changed in the meantime. Reload and try again."
            );
        }

        if (orderShipmentRepository.existsByOrderId(id)) {
            orderRepository.markManuallyManaged(id);
        }
    }

    @Transactional
    public void changeShipmentStatus(Long orderId, Long shipmentId, ShipmentStatus target) {
        OrderShipment shipment = orderShipmentRepository.findByIdAndOrderId(shipmentId, orderId)
                .orElseThrow(() -> new OrderNotFoundException("Parcel not found with id: " + shipmentId));

        if (!SHIPMENT_TRANSITIONS.getOrDefault(shipment.getStatus(), List.of()).contains(target)) {
            throw new InvalidOrderTransitionException(
                    "Parcel " + shipmentId + " cannot move from " + shipment.getStatus() + " to " + target + "."
            );
        }

        if (!orderFulfillmentService.advanceShipment(orderId, shipmentId, shipment.getStatus(), target)) {
            throw new InvalidOrderTransitionException(
                    "Parcel " + shipmentId + " was changed in the meantime. Reload and try again."
            );
        }

        orderRepository.markManuallyManaged(orderId);
    }
}
