package com.d4rksoul.base.ecommerce.spring.react.service;

import com.d4rksoul.base.ecommerce.spring.react.dto.result.StripeCheckoutEventResult;
import com.d4rksoul.base.ecommerce.spring.react.entity.Order;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderItem;
import com.d4rksoul.base.ecommerce.spring.react.enums.OrderStatus;
import com.d4rksoul.base.ecommerce.spring.react.repository.CartItemRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.CartRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class StripeWebhookService {

    private static final Set<String> SETTLED_PAYMENT_STATUSES = Set.of("paid", "no_payment_required");

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderStatusEventService orderStatusEventService;
    private final OrderService orderService;

    @Transactional
    public void handleEvent(StripeCheckoutEventResult event) {
        Order order = orderRepository.findByStripeCheckoutSessionId(event.sessionId()).orElse(null);
        if (order == null || order.getStatus() != OrderStatus.PENDING) {
            return;
        }

        switch (event.type()) {
            case "checkout.session.completed", "checkout.session.async_payment_succeeded" ->
                markAsPaidIfSettled(order, event);
            case "checkout.session.expired" ->
                orderService.releaseReservation(order, OrderStatus.CANCELED);
            case "checkout.session.async_payment_failed" ->
                orderService.releaseReservation(order, OrderStatus.FAILED);
            default -> {/**/}
        }
    }

    private void markAsPaidIfSettled(Order order, StripeCheckoutEventResult event) {
        if (!SETTLED_PAYMENT_STATUSES.contains(event.paymentStatus())) {
            return;
        }

        boolean transitioned = orderStatusEventService.transition(
            order.getId(), OrderStatus.PENDING, OrderStatus.PAID
        );
        if (!transitioned) {
            return;
        }

        order.setStatus(OrderStatus.PAID);
        order.setStripePaymentIntentId(event.paymentIntentId());
        order.setTaxAmount(event.taxAmount());
        order.setTotalAmount(event.totalAmount());
        order.setPaidAt(Instant.now());

        cartRepository.findByUserId(order.getUser().getId()).ifPresent(cart -> {
            for (OrderItem item : order.getOrderItems()) {
                cartItemRepository.deleteByCartIdAndProductSkuId(
                    cart.getId(), item.getProductSku().getId()
                );
            }

            cart.setDiscountCode(null);
        });
    }
}
