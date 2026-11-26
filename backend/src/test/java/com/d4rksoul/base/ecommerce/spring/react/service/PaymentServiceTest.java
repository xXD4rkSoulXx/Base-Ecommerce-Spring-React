package com.d4rksoul.base.ecommerce.spring.react.service;

import com.d4rksoul.base.ecommerce.spring.react.dto.request.CreateOrderRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.CheckoutSessionResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.result.StripeSessionResult;
import com.d4rksoul.base.ecommerce.spring.react.entity.Order;
import com.d4rksoul.base.ecommerce.spring.react.entity.User;
import com.d4rksoul.base.ecommerce.spring.react.integration.stripe.StripeApiClient;
import com.d4rksoul.base.ecommerce.spring.react.mapper.StripeMapper;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    private static final String CUSTOMER_ID = "cus_1";

    @Mock
    private OrderService orderService;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private StripeMapper stripeMapper;
    @Mock
    private StripeApiClient stripeApiClient;

    @InjectMocks
    private PaymentService service;

    private final CreateOrderRequest request = new CreateOrderRequest(null, "rate-1");

    @Test
    @DisplayName("checkout: a user without a Stripe customer gets one created and saved before the session")
    void checkoutCreatesCustomerForNewUser() {
        User user = User.builder()
            .email("ana@example.com")
            .build();

        Order order = Order.builder()
            .user(user)
            .build();

        StripeSessionResult session = new StripeSessionResult("cs_1", "https://stripe.test/pay");
        CheckoutSessionResponse response = mock(CheckoutSessionResponse.class);

        when(orderService.createPendingOrder(request)).thenReturn(order);
        when(stripeApiClient.createCustomer(user, order)).thenReturn(CUSTOMER_ID);
        when(stripeApiClient.createCheckoutSession(order, CUSTOMER_ID)).thenReturn(session);
        when(stripeMapper.toCheckoutSessionDto(session)).thenReturn(response);

        CheckoutSessionResponse result = service.createCheckoutSession(request);

        assertThat(result).isSameAs(response);
        assertThat(user.getStripeCustomerId()).isEqualTo(CUSTOMER_ID);
        assertThat(order.getStripeCheckoutSessionId()).isEqualTo("cs_1");

        verify(userRepository).save(user);
        verify(orderRepository).save(order);
        verify(stripeApiClient, never()).updateCustomerShipping(any(), any());
    }

    @Test
    @DisplayName("checkout: a user that already has a Stripe customer only gets the shipping address updated")
    void checkoutReusesExistingCustomer() {
        User user = User.builder()
            .stripeCustomerId(CUSTOMER_ID)
            .build();

        Order order = Order.builder()
            .user(user)
            .build();

        StripeSessionResult session = new StripeSessionResult("cs_1", "https://stripe.test/pay");
        CheckoutSessionResponse response = mock(CheckoutSessionResponse.class);

        when(orderService.createPendingOrder(request)).thenReturn(order);
        when(stripeApiClient.createCheckoutSession(order, CUSTOMER_ID)).thenReturn(session);
        when(stripeMapper.toCheckoutSessionDto(session)).thenReturn(response);

        CheckoutSessionResponse result = service.createCheckoutSession(request);

        assertThat(result).isSameAs(response);

        verify(stripeApiClient).updateCustomerShipping(CUSTOMER_ID, order);
        verify(stripeApiClient, never()).createCustomer(any(), any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("checkout: if Stripe fails, the pending order is cancelled and the error is rethrown")
    void checkoutCancelsOrderWhenStripeFails() {
        User user = User.builder()
            .stripeCustomerId(CUSTOMER_ID)
            .build();

        Order order = Order.builder()
            .user(user)
            .build();

        IllegalStateException failure = new IllegalStateException("Stripe is down");
        when(orderService.createPendingOrder(request)).thenReturn(order);
        when(stripeApiClient.createCheckoutSession(order, CUSTOMER_ID)).thenThrow(failure);

        assertThatThrownBy(() -> service.createCheckoutSession(request)).isSameAs(failure);

        verify(orderService).cancelOrder(order);
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("checkout: if the cancellation also fails, the original error is kept and the other is attached")
    void checkoutKeepsOriginalErrorWhenCancellationFails() {
        User user = User.builder()
            .stripeCustomerId(CUSTOMER_ID)
            .build();

        Order order = Order.builder()
            .user(user)
            .build();

        IllegalStateException failure = new IllegalStateException("Stripe is down");
        IllegalStateException cancelFailure = new IllegalStateException("Cancel failed");

        when(orderService.createPendingOrder(request)).thenReturn(order);
        when(stripeApiClient.createCheckoutSession(order, CUSTOMER_ID)).thenThrow(failure);
        doThrow(cancelFailure).when(orderService).cancelOrder(order);

        assertThatThrownBy(() -> service.createCheckoutSession(request)).isSameAs(failure);

        assertThat(failure.getSuppressed()).containsExactly(cancelFailure);
    }

    @Test
    @DisplayName("checkout: if the order cannot be created, Stripe is never contacted")
    void checkoutDoesNotContactStripeWhenOrderCreationFails() {
        IllegalStateException failure = new IllegalStateException("Out of stock");
        when(orderService.createPendingOrder(request)).thenThrow(failure);

        assertThatThrownBy(() -> service.createCheckoutSession(request)).isSameAs(failure);

        verifyNoInteractions(stripeApiClient, orderRepository, userRepository);
        verify(orderService, never()).cancelOrder(any());
    }
}
