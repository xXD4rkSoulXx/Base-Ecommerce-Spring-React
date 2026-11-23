package com.d4rksoul.base.ecommerce.spring.react.service.admin;

import com.d4rksoul.base.ecommerce.spring.react.dto.request.admin.AdminOrderSearchRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.OrderDetailsResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.OrderShipmentResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.PageResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminOrderDetailsResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminOrderSummaryResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.Order;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderShipment;
import com.d4rksoul.base.ecommerce.spring.react.enums.AuditAction;
import com.d4rksoul.base.ecommerce.spring.react.enums.AuditEntityType;
import com.d4rksoul.base.ecommerce.spring.react.enums.OrderStatus;
import com.d4rksoul.base.ecommerce.spring.react.enums.ShipmentStatus;
import com.d4rksoul.base.ecommerce.spring.react.exception.OrderNotFoundException;
import com.d4rksoul.base.ecommerce.spring.react.exception.InvalidOrderTransitionException;
import com.d4rksoul.base.ecommerce.spring.react.mapper.PageMapper;
import com.d4rksoul.base.ecommerce.spring.react.mapper.admin.AdminOrderMapper;
import com.d4rksoul.base.ecommerce.spring.react.pdf.ShippingSlipPdfGenerator;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderShipmentRepository;
import com.d4rksoul.base.ecommerce.spring.react.service.OrderService;
import com.d4rksoul.base.ecommerce.spring.react.service.OrderShipmentService;
import com.d4rksoul.base.ecommerce.spring.react.service.OrderStatusEventService;
import com.d4rksoul.base.ecommerce.spring.react.service.ShipmentTrackingEventService;
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
    private final OrderShipmentService orderShipmentService;
    private final OrderStatusEventService orderStatusEventService;
    private final ShipmentTrackingEventService shipmentTrackingEventService;
    private final AdminAuditLogService adminAuditLogService;
    private final AdminOrderMapper adminOrderMapper;
    private final PageMapper pageMapper;
    private final ShippingSlipPdfGenerator shippingSlipPdfGenerator;

    @Transactional(readOnly = true)
    public PageResponse<AdminOrderSummaryResponse> getOrders(AdminOrderSearchRequest filter) {
        Specification<Order> specification = Specification
            .where(OrderSpecification.hasStatusIn(
                Boolean.TRUE.equals(filter.pending())
                    ? OrderStatus.FULFILLMENT_STATUSES
                    : OrderStatus.VISIBLE_STATUSES
            ))
            .and(OrderSpecification.hasStatus(filter.status()))
            .and(OrderSpecification.createdFrom(filter.from()))
            .and(OrderSpecification.createdTo(filter.to()))
            .and(OrderSpecification.matchesCustomerSearch(filter.q()))
            .and(Boolean.TRUE.equals(filter.pending())
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
        Order order = orderRepository.findByIdAndStatusIn(id, OrderStatus.VISIBLE_STATUSES)
            .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));

        OrderDetailsResponse details = orderService.buildDetails(order);
        OrderStatus nextStatus = order.getStatus() == OrderStatus.PAID ? OrderStatus.PROCESSING : null;

        Map<Long, List<ShipmentStatus>> shipmentActions = details.shipments().stream()
            .collect(Collectors.toMap(
                OrderShipmentResponse::id,
                shipment ->
                    SHIPMENT_TRANSITIONS.getOrDefault(shipment.status(), List.of())
            ));

        return adminOrderMapper.toDetailsDto(order, details, nextStatus, shipmentActions);
    }

    @Transactional(readOnly = true)
    public byte[] getShippingSlip(Long id) {
        Order order = orderRepository.findByIdAndStatusIn(id, OrderStatus.HISTORY_STATUSES)
            .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));

        List<OrderShipment> shipments = orderShipmentRepository.findByOrderIdOrderByIdAsc(id);

        return shippingSlipPdfGenerator.generateShippingSlip(order, shipments);
    }

    @Transactional
    public void changeStatus(Long id, OrderStatus target) {
        Order order = orderRepository.findByIdAndStatusIn(id, OrderStatus.VISIBLE_STATUSES)
            .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));

        if (order.getStatus() != OrderStatus.PAID || target != OrderStatus.PROCESSING) {
            throw new InvalidOrderTransitionException(
                "Order " + id + " cannot move from " + order.getStatus() + " to " + target + "."
            );
        }

        boolean transitioned = orderStatusEventService
            .transition(id, OrderStatus.PAID, OrderStatus.PROCESSING);
        if (!transitioned) {
            throw new InvalidOrderTransitionException(
                "Order " + id + " was changed in the meantime. Reload and try again."
            );
        }

        orderShipmentService.purchaseLabels(id);

        boolean hasShipments = orderShipmentRepository.existsByOrderId(id);
        if (hasShipments) {
            orderRepository.markManuallyManaged(id);
        }

        adminAuditLogService.register(
            AuditAction.STATUS_CHANGED,
            AuditEntityType.ORDER,
            id,
            "#" + id,
            order.getStatus() + " → " + target
        );
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

        boolean transitioned = shipmentTrackingEventService
            .transition(shipmentId, shipment.getStatus(), target);
        if (!transitioned) {
            throw new InvalidOrderTransitionException(
                "Parcel " + shipmentId + " was changed in the meantime. Reload and try again."
            );
        }

        if (target == ShipmentStatus.IN_TRANSIT) {
            orderStatusEventService.transition(orderId, OrderStatus.PROCESSING, OrderStatus.SHIPPED);
        }

        if (target == ShipmentStatus.DELIVERED) {
            boolean hasUndeliveredShipments = orderShipmentRepository
                .existsByOrderIdAndStatusNot(orderId, ShipmentStatus.DELIVERED);
            if (!hasUndeliveredShipments) {
                orderStatusEventService.transition(orderId, OrderStatus.SHIPPED, OrderStatus.DELIVERED);
            }
        }

        orderRepository.markManuallyManaged(orderId);

        adminAuditLogService.register(
            AuditAction.SHIPMENT_STATUS_CHANGED,
            AuditEntityType.ORDER,
            orderId,
            "#" + orderId,
            "Parcel " + shipmentId + ": " + shipment.getStatus() + " → " + target
        );
    }
}
