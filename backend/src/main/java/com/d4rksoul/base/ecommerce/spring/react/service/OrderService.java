package com.d4rksoul.base.ecommerce.spring.react.service;

import com.d4rksoul.base.ecommerce.spring.react.dto.request.CheckoutAddressRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.CreateOrderRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.OrderSearchRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.OrderDetailsResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.OrderReturnResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.OrderSummaryResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.PageResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.ShippingRateResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.result.ReturnEligibilityResult;
import com.d4rksoul.base.ecommerce.spring.react.entity.Cart;
import com.d4rksoul.base.ecommerce.spring.react.entity.CartItem;
import com.d4rksoul.base.ecommerce.spring.react.entity.DiscountCode;
import com.d4rksoul.base.ecommerce.spring.react.entity.Order;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderItem;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderShipment;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderShipmentItem;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderStatusEvent;
import com.d4rksoul.base.ecommerce.spring.react.entity.ProductSku;
import com.d4rksoul.base.ecommerce.spring.react.entity.ShipmentTrackingEvent;
import com.d4rksoul.base.ecommerce.spring.react.entity.User;
import com.d4rksoul.base.ecommerce.spring.react.enums.OrderStatus;
import com.d4rksoul.base.ecommerce.spring.react.exception.EmptyCartException;
import com.d4rksoul.base.ecommerce.spring.react.exception.InsufficientStockException;
import com.d4rksoul.base.ecommerce.spring.react.exception.InvalidDiscountCodeException;
import com.d4rksoul.base.ecommerce.spring.react.exception.InvalidShippingRateException;
import com.d4rksoul.base.ecommerce.spring.react.exception.OrderNotFoundException;
import com.d4rksoul.base.ecommerce.spring.react.integration.stripe.StripeApiClient;
import com.d4rksoul.base.ecommerce.spring.react.mapper.OrderMapper;
import com.d4rksoul.base.ecommerce.spring.react.mapper.PageMapper;
import com.d4rksoul.base.ecommerce.spring.react.repository.CartItemRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.CartRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderShipmentItemRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderShipmentRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderStatusEventRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.ProductSkuRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.ShipmentTrackingEventRepository;
import com.d4rksoul.base.ecommerce.spring.react.specification.OrderSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    private final ProductSkuRepository productSkuRepository;
    private final UserService userService;
    private final ProductImageService productImageService;
    private final ShippingRateService shippingRateService;
    private final OrderReturnService orderReturnService;
    private final OrderMapper orderMapper;
    private final PageMapper pageMapper;
    private final StripeApiClient stripeApiClient;

    @Value("${STRIPE_CURRENCY}")
    private String currency;

    @Transactional
    public Order createPendingOrder(CreateOrderRequest request) {
        User user = userService.getCurrentUser();
        Cart cart = cartRepository.findByUserIdForUpdate(user.getId())
                .orElseThrow(() -> new EmptyCartException("Your cart is empty."));

        List<CartItem> cartItems = cartItemRepository.findByCartIdOrderByIdAsc(cart.getId());
        if (cartItems.isEmpty()) {
            throw new EmptyCartException("Your cart is empty.");
        }

        CheckoutAddressRequest address = request.address();
        ShippingRateResponse rate = shippingRateService.getShippingRates(address).stream()
                .filter(r -> r.rateId().equals(request.rateId()))
                .findFirst()
                .orElseThrow(() -> new InvalidShippingRateException(
                        "The selected shipping option is no longer available."
                ));

        if (!rate.currency().equalsIgnoreCase(currency)) {
            throw new InvalidShippingRateException("Shipping rate currency does not match the store currency.");
        }

        DiscountCode discountCode = Optional.ofNullable(cart.getDiscountCode())
                .filter(DiscountCode::isActive)
                .orElse(null);

        boolean discountCodeAlreadyUsed = discountCode != null
                && orderRepository.existsByUserIdAndDiscountCodeIdAndStatusNotIn(
                    user.getId(),
                    discountCode.getId(),
                    List.of(OrderStatus.PENDING, OrderStatus.FAILED, OrderStatus.CANCELED, OrderStatus.REFUNDED)
        );

        if (discountCodeAlreadyUsed) {
            throw new InvalidDiscountCodeException("You have already used this discount code.");
        }

        for (Order previous : orderRepository.findByUserIdAndStatus(user.getId(), OrderStatus.PENDING)) {
            stripeApiClient.expireSessionUnlessExpired(previous.getStripeCheckoutSessionId());
            releaseReservation(previous, OrderStatus.CANCELED);
        }

        for (CartItem cartItem : cartItems) {
            ProductSku sku = cartItem.getProductSku();

            if (productSkuRepository.decrementStock(sku.getId(), cartItem.getQuantity()) == 0) {
                throw new InsufficientStockException(
                        "Not enough stock for " + sku.getProduct().getName() + "."
                );
            }
        }

        Map<Long, String> covers = productImageService.findCoversBySkuId(
                cartItems.stream().map(CartItem::getProductSku).toList()
        );

        Order order = orderMapper.toEntity(user, discountCode, currency, address, rate, cartItems, covers);

        return orderRepository.save(order);
    }

    public void releaseReservation(Order order, OrderStatus newStatus) {
        if (orderRepository.updateStatus(order.getId(), OrderStatus.PENDING, newStatus) == 1) {
            for (OrderItem item : order.getOrderItems()) {
                productSkuRepository.incrementStock(item.getProductSku().getId(), item.getQuantity());
            }
        }
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

    public OrderDetailsResponse buildDetails(Order order) {
        List<OrderShipment> shipments = orderShipmentRepository.findByOrderIdOrderByIdAsc(order.getId());

        List<OrderStatusEvent> events = orderStatusEventRepository.findByOrderIdOrderByOccurredAtAscIdAsc(order.getId());

        List<ShipmentTrackingEvent> shipmentEvents =
                shipmentTrackingEventRepository.findByShipmentInOrderByOccurredAtAscIdAsc(shipments);

        List<OrderShipmentItem> shipmentItems = orderShipmentItemRepository.findByShipmentInOrderByIdAsc(shipments);

        List<OrderReturnResponse> returns = orderReturnService.getReturns(order.getId());
        ReturnEligibilityResult eligibility = orderReturnService.getEligibility(order, events);

        return orderMapper.toDetailsDto(
                order, shipments, shipmentItems, shipmentEvents, events, returns, eligibility
        );
    }

    @Transactional
    public void cancelOrder(Order order) {
        releaseReservation(order, OrderStatus.CANCELED);
    }
}
