package com.d4rksoul.base.ecommerce.spring.react.service;

import com.d4rksoul.base.ecommerce.spring.react.dto.result.StripeCheckoutEventResult;
import com.d4rksoul.base.ecommerce.spring.react.entity.Cart;
import com.d4rksoul.base.ecommerce.spring.react.entity.DiscountCode;
import com.d4rksoul.base.ecommerce.spring.react.entity.Order;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderItem;
import com.d4rksoul.base.ecommerce.spring.react.entity.ProductSku;
import com.d4rksoul.base.ecommerce.spring.react.entity.User;
import com.d4rksoul.base.ecommerce.spring.react.enums.OrderStatus;
import com.d4rksoul.base.ecommerce.spring.react.repository.CartItemRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.CartRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StripeWebhookServiceTest {

    private static final String SESSION_ID = "cs_1";
    private static final long ORDER_ID = 100L;
    private static final long USER_ID = 1L;
    private static final long CART_ID = 2L;

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private OrderStatusEventService orderStatusEventService;
    @Mock
    private OrderService orderService;

    @InjectMocks
    private StripeWebhookService service;

    private final User user = User.builder()
        .id(USER_ID)
        .build();

    private final DiscountCode discountCode = DiscountCode.builder()
        .id(5L)
        .code("SAVE")
        .build();

    private final Cart cart = Cart.builder()
        .id(CART_ID)
        .user(user)
        .discountCode(discountCode)
        .build();

    private final Order order = Order.builder()
        .id(ORDER_ID)
        .status(OrderStatus.PENDING)
        .user(user)
        .orderItems(new ArrayList<>(List.of(
            OrderItem.builder()
                .productSku(
                    ProductSku.builder()
                        .id(3L)
                        .build()
                )
                .quantity(1)
                .build(),
            OrderItem.builder()
                .productSku(
                    ProductSku.builder()
                        .id(4L)
                        .build()
                )
                .quantity(2)
                .build()
        )))
        .build();

    @Test
    @DisplayName("ignores an event for a session that matches no order")
    void ignoresUnknownSession() {
        when(orderRepository.findByStripeCheckoutSessionId(SESSION_ID)).thenReturn(Optional.empty());

        service.handleEvent(event("checkout.session.completed", "paid"));

        verifyNoInteractions(orderStatusEventService, orderService, cartRepository, cartItemRepository);
    }

    @Test
    @DisplayName("ignores an event for an order that is no longer pending (Stripe retries events)")
    void ignoresOrderThatIsNotPending() {
        order.setStatus(OrderStatus.PAID);

        givenOrder();

        service.handleEvent(event("checkout.session.completed", "paid"));

        verifyNoInteractions(orderStatusEventService, orderService, cartRepository, cartItemRepository);
    }

    @Test
    @DisplayName("completed and paid: marks the order as paid, stores the payment data and clears the cart")
    void completedPaidMarksOrderPaidAndClearsCart() {
        givenOrder();
        givenTransitionToPaid(true);

        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));

        service.handleEvent(event("checkout.session.completed", "paid"));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(order.getStripePaymentIntentId()).isEqualTo("pi_1");
        assertThat(order.getTaxAmount()).isEqualByComparingTo("2.30");
        assertThat(order.getTotalAmount()).isEqualByComparingTo("52.30");
        assertThat(order.getPaidAt()).isNotNull();

        verify(cartItemRepository).deleteByCartIdAndProductSkuId(CART_ID, 3L);
        verify(cartItemRepository).deleteByCartIdAndProductSkuId(CART_ID, 4L);

        assertThat(cart.getDiscountCode()).isNull();
    }

    @Test
    @DisplayName("async payment succeeded: marks the order as paid")
    void asyncPaymentSucceededMarksOrderPaid() {
        givenOrder();
        givenTransitionToPaid(true);

        service.handleEvent(event("checkout.session.async_payment_succeeded", "paid"));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    @DisplayName("no payment required (free order): marks the order as paid")
    void noPaymentRequiredMarksOrderPaid() {
        givenOrder();
        givenTransitionToPaid(true);

        service.handleEvent(event("checkout.session.completed", "no_payment_required"));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    @DisplayName("completed but not paid yet (delayed payment method): keeps the order pending")
    void unpaidCompletedSessionKeepsOrderPending() {
        givenOrder();

        service.handleEvent(event("checkout.session.completed", "unpaid"));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);

        verifyNoInteractions(orderStatusEventService, cartRepository, cartItemRepository);
    }

    @Test
    @DisplayName("changes nothing when the order was already moved by another request")
    void lostTransitionRaceChangesNothing() {
        givenOrder();
        givenTransitionToPaid(false);

        service.handleEvent(event("checkout.session.completed", "paid"));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getPaidAt()).isNull();

        verifyNoInteractions(cartRepository, cartItemRepository);
    }

    @Test
    @DisplayName("marks the order as paid even when the user has no cart")
    void paidWithoutCartStillMarksOrderPaid() {
        givenOrder();
        givenTransitionToPaid(true);

        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        service.handleEvent(event("checkout.session.completed", "paid"));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);

        verifyNoInteractions(cartItemRepository);
    }

    @Test
    @DisplayName("session expired: releases the reserved stock and cancels the order")
    void expiredReleasesReservationAsCanceled() {
        givenOrder();

        service.handleEvent(event("checkout.session.expired", "unpaid"));

        verify(orderService).releaseReservation(order, OrderStatus.CANCELED);
        verify(orderStatusEventService, never()).transition(ORDER_ID, OrderStatus.PENDING, OrderStatus.PAID);
    }

    @Test
    @DisplayName("async payment failed: releases the reserved stock and marks the order as failed")
    void asyncPaymentFailedReleasesReservationAsFailed() {
        givenOrder();

        service.handleEvent(event("checkout.session.async_payment_failed", "unpaid"));

        verify(orderService).releaseReservation(order, OrderStatus.FAILED);
    }

    @Test
    @DisplayName("ignores other event types")
    void ignoresOtherEventTypes() {
        givenOrder();

        service.handleEvent(event("checkout.session.something_else", "paid"));

        verifyNoInteractions(orderStatusEventService, orderService, cartRepository, cartItemRepository);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    private void givenOrder() {
        when(orderRepository.findByStripeCheckoutSessionId(SESSION_ID)).thenReturn(Optional.of(order));
    }

    private void givenTransitionToPaid(boolean transitioned) {
        when(orderStatusEventService.transition(ORDER_ID, OrderStatus.PENDING, OrderStatus.PAID))
            .thenReturn(transitioned);
    }

    private static StripeCheckoutEventResult event(String type, String paymentStatus) {
        return StripeCheckoutEventResult.builder()
            .type(type)
            .sessionId(SESSION_ID)
            .paymentStatus(paymentStatus)
            .paymentIntentId("pi_1")
            .taxAmount(new BigDecimal("2.30"))
            .totalAmount(new BigDecimal("52.30"))
            .build();
    }
}
