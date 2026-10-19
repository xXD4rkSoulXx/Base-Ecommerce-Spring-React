package com.d4rksoul.base.ecommerce.spring.react.service;

import com.d4rksoul.base.ecommerce.spring.react.dto.request.CheckoutAddressRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.CreateOrderRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.ShippingRateResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.Cart;
import com.d4rksoul.base.ecommerce.spring.react.entity.CartItem;
import com.d4rksoul.base.ecommerce.spring.react.entity.DiscountCode;
import com.d4rksoul.base.ecommerce.spring.react.entity.Order;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderItem;
import com.d4rksoul.base.ecommerce.spring.react.entity.ProductSku;
import com.d4rksoul.base.ecommerce.spring.react.entity.User;
import com.d4rksoul.base.ecommerce.spring.react.enums.OrderStatus;
import com.d4rksoul.base.ecommerce.spring.react.exception.EmptyCartException;
import com.d4rksoul.base.ecommerce.spring.react.exception.InsufficientStockException;
import com.d4rksoul.base.ecommerce.spring.react.exception.InvalidDiscountCodeException;
import com.d4rksoul.base.ecommerce.spring.react.exception.InvalidShippingRateException;
import com.d4rksoul.base.ecommerce.spring.react.exception.UserNotAuthenticatedException;
import com.d4rksoul.base.ecommerce.spring.react.integration.stripe.StripeApiClient;
import com.d4rksoul.base.ecommerce.spring.react.mapper.OrderMapper;
import com.d4rksoul.base.ecommerce.spring.react.repository.CartItemRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.CartRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.ProductSkuRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductSkuRepository productSkuRepository;
    private final ProductImageService productImageService;
    private final ShippingRateService shippingRateService;
    private final OrderMapper orderMapper;
    private final StripeApiClient stripeApiClient;

    @Value("${STRIPE_CURRENCY}")
    private String currency;

    @Transactional
    public Order createPendingOrder(CreateOrderRequest request) {
        User user = getCurrentUser();
        Cart cart = cartRepository.findByUserIdForUpdate(user.getId())
                .orElseThrow(() -> new EmptyCartException("Your cart is empty."));

        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
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

        if (discountCode != null && existsUserUsedDiscountCode(user.getId(), discountCode.getId())) {
            throw new InvalidDiscountCodeException("You have already used this discount code.");
        }

        cancelPendingOrders(user.getId());

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

    private void cancelPendingOrders(Long userId) {
        for (Order previous : orderRepository.findByUserIdAndStatus(userId, OrderStatus.PENDING)) {
            stripeApiClient.expireSessionUnlessExpired(previous.getStripeCheckoutSessionId());
            releaseReservation(previous, OrderStatus.CANCELED);
        }
    }

    public void releaseReservation(Order order, OrderStatus newStatus) {
        if (orderRepository.updateStatus(order.getId(), OrderStatus.PENDING, newStatus) == 1) {
            for (OrderItem item : order.getOrderItems()) {
                productSkuRepository.incrementStock(item.getProductSku().getId(), item.getQuantity());
            }
        }
    }

    @Transactional
    public void cancelOrder(Order order) {
        releaseReservation(order, OrderStatus.CANCELED);
    }

    public boolean existsUserUsedDiscountCode(Long userId, Long discountCodeId) {
        return orderRepository.existsByUserIdAndDiscountCodeIdAndStatusNotIn(
                userId,
                discountCodeId,
                List.of(OrderStatus.PENDING, OrderStatus.FAILED, OrderStatus.CANCELED)
        );
    }

    private User getCurrentUser() {
        Jwt jwt = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(auth -> (Jwt) auth.getPrincipal())
                .orElseThrow(() -> new UserNotAuthenticatedException("User is not authenticated"));

        return userRepository.findByEmail(jwt.getSubject())
                .orElseThrow(() -> new UserNotAuthenticatedException("User is not authenticated"));
    }
}
