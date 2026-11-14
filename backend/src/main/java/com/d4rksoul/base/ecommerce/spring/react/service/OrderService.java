package com.d4rksoul.base.ecommerce.spring.react.service;

import com.d4rksoul.base.ecommerce.spring.react.constant.Constants;
import com.d4rksoul.base.ecommerce.spring.react.config.StripeProperties;
import com.d4rksoul.base.ecommerce.spring.react.dto.projection.ReturnedQuantityProjection;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.CreateOrderRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.OrderSearchRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.OrderDetailsResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.OrderItemResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.OrderReturnResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.OrderShipmentResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.OrderStatusEventResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.OrderSummaryResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.PageResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.ShippingRateResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.Cart;
import com.d4rksoul.base.ecommerce.spring.react.entity.CartItem;
import com.d4rksoul.base.ecommerce.spring.react.entity.DiscountCode;
import com.d4rksoul.base.ecommerce.spring.react.entity.Order;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderItem;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderReturn;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderReturnItem;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderShipment;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderShipmentItem;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderStatusEvent;
import com.d4rksoul.base.ecommerce.spring.react.entity.ShipmentTrackingEvent;
import com.d4rksoul.base.ecommerce.spring.react.entity.User;
import com.d4rksoul.base.ecommerce.spring.react.enums.OrderStatus;
import com.d4rksoul.base.ecommerce.spring.react.enums.ReturnStatus;
import com.d4rksoul.base.ecommerce.spring.react.exception.EmptyCartException;
import com.d4rksoul.base.ecommerce.spring.react.exception.InsufficientStockException;
import com.d4rksoul.base.ecommerce.spring.react.exception.InvalidDiscountCodeException;
import com.d4rksoul.base.ecommerce.spring.react.exception.InvalidShippingRateException;
import com.d4rksoul.base.ecommerce.spring.react.exception.OrderNotFoundException;
import com.d4rksoul.base.ecommerce.spring.react.exception.ProductUnavailableException;
import com.d4rksoul.base.ecommerce.spring.react.integration.stripe.StripeApiClient;
import com.d4rksoul.base.ecommerce.spring.react.mapper.OrderItemMapper;
import com.d4rksoul.base.ecommerce.spring.react.mapper.OrderMapper;
import com.d4rksoul.base.ecommerce.spring.react.mapper.OrderReturnMapper;
import com.d4rksoul.base.ecommerce.spring.react.mapper.OrderShipmentMapper;
import com.d4rksoul.base.ecommerce.spring.react.mapper.OrderStatusEventMapper;
import com.d4rksoul.base.ecommerce.spring.react.mapper.PageMapper;
import com.d4rksoul.base.ecommerce.spring.react.repository.CartItemRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.CartRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderReturnItemRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderReturnRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderShipmentItemRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderShipmentRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderStatusEventRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.ProductSkuRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.ShipmentTrackingEventRepository;
import com.d4rksoul.base.ecommerce.spring.react.specification.OrderSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.d4rksoul.base.ecommerce.spring.react.enums.OrderStatus.HISTORY_STATUSES;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final OrderShipmentRepository orderShipmentRepository;
    private final OrderShipmentItemRepository orderShipmentItemRepository;
    private final OrderStatusEventRepository orderStatusEventRepository;
    private final ShipmentTrackingEventRepository shipmentTrackingEventRepository;
    private final OrderReturnRepository orderReturnRepository;
    private final OrderReturnItemRepository orderReturnItemRepository;
    private final ProductSkuRepository productSkuRepository;
    private final UserService userService;
    private final ProductSkuImageService productSkuImageService;
    private final ShippingRateService shippingRateService;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderStatusEventMapper orderStatusEventMapper;
    private final OrderShipmentMapper orderShipmentMapper;
    private final OrderReturnMapper orderReturnMapper;
    private final PageMapper pageMapper;
    private final StripeApiClient stripeApiClient;
    private final StripeProperties stripeProperties;

    @Transactional
    public Order createPendingOrder(CreateOrderRequest request) {
        User user = userService.getCurrentUser();
        Cart cart = cartRepository.findByUserIdForUpdate(user.getId())
            .orElseThrow(() -> new EmptyCartException("Your cart is empty."));

        List<CartItem> cartItems = cartItemRepository.findByCartIdOrderByIdAsc(cart.getId());
        if (cartItems.isEmpty()) {
            throw new EmptyCartException("Your cart is empty.");
        }

        for (CartItem cartItem : cartItems) {
            if (!cartItem.getProductSku().isActive() || !cartItem.getProductSku().getProduct().isActive()) {
                throw new ProductUnavailableException(
                    cartItem.getProductSku().getProduct().getName()
                        + " is no longer available. Remove it from your cart to continue."
                );
            }
        }

        ShippingRateResponse rate = shippingRateService
            .getShippingRates(request.address())
            .stream()
            .filter(r -> r.rateId().equals(request.rateId()))
            .findFirst()
            .orElseThrow(() -> new InvalidShippingRateException(
                "The selected shipping option is no longer available."
            ));

        if (!rate.currency().equalsIgnoreCase(stripeProperties.currency())) {
            throw new InvalidShippingRateException(
                "Shipping rate currency does not match the store currency."
            );
        }

        DiscountCode discountCode = Optional.ofNullable(cart.getDiscountCode())
            .filter(DiscountCode::isActive)
            .orElse(null);

        if (discountCode != null) {
            boolean discountCodeAlreadyUsed = orderRepository.existsByUserIdAndDiscountCodeIdAndStatusNotIn(
                user.getId(), discountCode.getId(), OrderStatus.DISCOUNT_UNUSED_STATUSES
            );
            if (discountCodeAlreadyUsed) {
                throw new InvalidDiscountCodeException("You have already used this discount code.");
            }
        }

        List<Order> pendingOrders = orderRepository.findByUserIdAndStatus(user.getId(), OrderStatus.PENDING);
        for (Order previous : pendingOrders) {
            stripeApiClient.expireSession(previous.getStripeCheckoutSessionId());
            releaseReservation(previous, OrderStatus.CANCELED);
        }

        for (CartItem cartItem : cartItems) {
            int updatedRows = productSkuRepository.decrementStock(
                cartItem.getProductSku().getId(), cartItem.getQuantity()
            );
            if (updatedRows == 0) {
                throw new InsufficientStockException(
                    "Not enough stock for " + cartItem.getProductSku().getProduct().getName() + "."
                );
            }
        }

        Map<Long, String> covers = productSkuImageService.findCoversBySkuId(
            cartItems.stream().map(CartItem::getProductSku).toList()
        );

        Order order = orderMapper.toEntity(
            user, discountCode, stripeProperties.currency(), request.address(), rate
        );

        for (CartItem cartItem : cartItems) {
            order.getOrderItems().add(
                orderItemMapper.toEntity(order, cartItem, covers.get(cartItem.getProductSku().getId()))
            );
        }

        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderSummaryResponse> getOrders(OrderSearchRequest filter) {
        User user = userService.getCurrentUser();

        Specification<Order> specification = Specification
            .where(OrderSpecification.belongsToUser(user.getId()))
            .and(OrderSpecification.hasStatusIn(HISTORY_STATUSES))
            .and(OrderSpecification.createdFrom(filter.from()))
            .and(OrderSpecification.createdTo(filter.to()))
            .and(OrderSpecification.matchesSearch(filter.q()))
            .and(OrderSpecification.orderByRecentDesc());

        Pageable pageable = PageRequest.of(filter.page(), filter.size());
        Page<OrderSummaryResponse> ordersPage = orderRepository.findAll(specification, pageable)
            .map(orderMapper::toSummaryDto);

        return pageMapper.toDto(ordersPage);
    }

    @Transactional(readOnly = true)
    public OrderDetailsResponse getOrder(Long id) {
        User user = userService.getCurrentUser();

        Order order = orderRepository.findByIdAndUserIdAndStatusIn(id, user.getId(), HISTORY_STATUSES)
            .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));

        return buildDetails(order);
    }

    @Transactional
    public void cancelOrder(Order order) {
        releaseReservation(order, OrderStatus.CANCELED);
    }

    public void releaseReservation(Order order, OrderStatus newStatus) {
        int updatedRows = orderRepository.updateStatus(order.getId(), OrderStatus.PENDING, newStatus);
        if (updatedRows == 1) {
            for (OrderItem item : order.getOrderItems()) {
                productSkuRepository.incrementStock(item.getProductSku().getId(), item.getQuantity());
            }
        }
    }

    public OrderDetailsResponse buildDetails(Order order) {
        List<OrderShipment> shipments = orderShipmentRepository.findByOrderIdOrderByIdAsc(order.getId());

        List<OrderStatusEvent> events = orderStatusEventRepository
            .findByOrderIdOrderByOccurredAtAscIdAsc(order.getId());

        List<ShipmentTrackingEvent> shipmentEvents =
            shipmentTrackingEventRepository.findByShipmentInOrderByOccurredAtAscIdAsc(shipments);

        List<OrderShipmentItem> shipmentItems = orderShipmentItemRepository
            .findByShipmentInOrderByIdAsc(shipments);

        Map<Long, List<OrderShipmentItem>> itemsByShipmentId = shipmentItems.stream()
            .collect(Collectors.groupingBy(item -> item.getShipment().getId()));
        Map<Long, List<ShipmentTrackingEvent>> eventsByShipmentId = shipmentEvents.stream()
            .collect(Collectors.groupingBy(event -> event.getShipment().getId()));

        List<OrderShipmentResponse> shipmentResponses = shipments.stream()
            .map(shipment -> orderShipmentMapper.toDto(
                shipment,
                itemsByShipmentId.getOrDefault(shipment.getId(), List.of()),
                eventsByShipmentId.getOrDefault(shipment.getId(), List.of())
            ))
            .toList();

        List<OrderReturn> orderReturns = orderReturnRepository
            .findByOrderIdOrderByCreatedAtAscIdAsc(order.getId());
        List<OrderReturnItem> returnItems = orderReturnItemRepository
            .findByOrderReturnInOrderByIdAsc(orderReturns);

        Map<Long, List<OrderReturnItem>> returnItemsByReturnId = returnItems.stream()
            .collect(Collectors.groupingBy(item -> item.getOrderReturn().getId()));

        List<OrderReturnResponse> returns = orderReturns.stream()
            .map(orderReturn -> orderReturnMapper.toDto(
                orderReturn,
                returnItemsByReturnId.getOrDefault(orderReturn.getId(), List.of())
            ))
            .toList();

        Instant returnableUntil = events.stream()
            .filter(event -> event.getStatus() == OrderStatus.DELIVERED)
            .map(OrderStatusEvent::getOccurredAt)
            .findFirst()
            .map(deliveredAt -> deliveredAt.plus(Constants.RETURN_WINDOW))
            .filter(deadline ->
                order.getStatus() == OrderStatus.DELIVERED && !Instant.now().isAfter(deadline)
            )
            .orElse(null);

        Map<Long, Long> returnableByItemId;
        if (returnableUntil == null) {
            returnableByItemId = Map.of();
        } else {
            Map<Long, Long> returnedByItemId = orderReturnItemRepository
                .sumReturnedQuantities(
                    order.getId(), ReturnStatus.INACTIVE_STATUSES
                )
                .stream()
                .collect(Collectors.toMap(
                    ReturnedQuantityProjection::orderItemId,
                    ReturnedQuantityProjection::quantity
                ));

            returnableByItemId = order.getOrderItems().stream()
                .collect(Collectors.toMap(
                    OrderItem::getId,
                    item ->
                        item.getQuantity() - returnedByItemId.getOrDefault(item.getId(), 0L)
                ));
        }

        List<OrderStatusEventResponse> eventResponses = events.stream()
            .map(orderStatusEventMapper::toDto)
            .toList();

        List<OrderItemResponse> items = order.getOrderItems().stream()
            .map(item -> orderItemMapper.toDto(
                item,
                returnableByItemId.getOrDefault(item.getId(), 0L).intValue()
            ))
            .toList();

        BigDecimal subtotal = items.stream()
            .map(OrderItemResponse::lineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discountAmount = subtotal
            .add(order.getShippingCost())
            .add(order.getTaxAmount())
            .subtract(order.getTotalAmount());

        return orderMapper.toDetailsDto(
            order,
            items,
            subtotal,
            discountAmount,
            eventResponses,
            shipmentResponses,
            returns,
            returnableUntil
        );
    }
}
