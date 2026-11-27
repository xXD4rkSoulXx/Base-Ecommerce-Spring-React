package com.d4rksoul.base.ecommerce.spring.react.scheduler;

import com.d4rksoul.base.ecommerce.spring.react.constant.Constants;
import com.d4rksoul.base.ecommerce.spring.react.entity.Order;
import com.d4rksoul.base.ecommerce.spring.react.enums.OrderStatus;
import com.d4rksoul.base.ecommerce.spring.react.integration.stripe.StripeApiClient;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderExpirationSchedulerTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private StripeApiClient stripeApiClient;
    @Mock
    private RequestActivityFilter activityFilter;

    @InjectMocks
    private OrderExpirationScheduler scheduler;

    @Test
    @DisplayName("expire: expires the Stripe session of every stale pending order")
    void expiresSessionOfEachStaleOrder() {
        when(activityFilter.isActive()).thenReturn(true);
        when(orderRepository.findByStatusAndStripeCheckoutSessionIdNotNullAndCreatedAtBefore(
            any(OrderStatus.class), any(Instant.class)
        )).thenReturn(List.of(order("cs_1"), order("cs_2")));

        scheduler.expireStaleOrders();

        verify(stripeApiClient).expireSession("cs_1");
        verify(stripeApiClient).expireSession("cs_2");
    }

    @Test
    @DisplayName("expire: a failure on one order does not stop the others")
    void failureOnOneOrderDoesNotStopTheOthers() {
        when(activityFilter.isActive()).thenReturn(true);
        when(orderRepository.findByStatusAndStripeCheckoutSessionIdNotNullAndCreatedAtBefore(
            any(OrderStatus.class), any(Instant.class)
        )).thenReturn(List.of(order("cs_1"), order("cs_2")));
        doThrow(new IllegalStateException("Stripe is down"))
            .when(stripeApiClient).expireSession("cs_1");

        scheduler.expireStaleOrders();

        verify(stripeApiClient).expireSession("cs_2");
    }

    @Test
    @DisplayName("expire: with no stale orders Stripe is never contacted")
    void doesNothingWithoutStaleOrders() {
        when(activityFilter.isActive()).thenReturn(true);
        when(orderRepository.findByStatusAndStripeCheckoutSessionIdNotNullAndCreatedAtBefore(
            any(OrderStatus.class), any(Instant.class)
        )).thenReturn(List.of());

        scheduler.expireStaleOrders();

        verifyNoInteractions(stripeApiClient);
    }

    @Test
    @DisplayName("expire: only looks at PENDING orders older than the pending order timeout")
    void looksOnlyAtPendingOrdersOlderThanTimeout() {
        when(activityFilter.isActive()).thenReturn(true);
        ArgumentCaptor<Instant> limitCaptor = ArgumentCaptor.forClass(Instant.class);
        when(orderRepository.findByStatusAndStripeCheckoutSessionIdNotNullAndCreatedAtBefore(
            any(OrderStatus.class), any(Instant.class)
        )).thenReturn(List.of());

        Instant before = Instant.now();
        scheduler.expireStaleOrders();
        Instant after = Instant.now();

        verify(orderRepository).findByStatusAndStripeCheckoutSessionIdNotNullAndCreatedAtBefore(
            org.mockito.ArgumentMatchers.eq(OrderStatus.PENDING), limitCaptor.capture()
        );

        assertThat(limitCaptor.getValue()).isBetween(
            before.minus(Constants.PENDING_ORDER_TIMEOUT),
            after.minus(Constants.PENDING_ORDER_TIMEOUT)
        );
    }

    @Test
    @DisplayName("expire: does nothing while there has been no recent activity")
    void doesNothingWithoutRecentActivity() {
        when(activityFilter.isActive()).thenReturn(false);

        scheduler.expireStaleOrders();

        verifyNoInteractions(orderRepository, stripeApiClient);
    }

    private Order order(String sessionId) {
        return Order.builder()
            .stripeCheckoutSessionId(sessionId)
            .build();
    }
}
