package com.d4rksoul.base.ecommerce.spring.react.service;

import com.d4rksoul.base.ecommerce.spring.react.dto.request.CreateOrderRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.CheckoutSessionResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.result.StripeSessionResult;
import com.d4rksoul.base.ecommerce.spring.react.entity.Order;
import com.d4rksoul.base.ecommerce.spring.react.entity.User;
import com.d4rksoul.base.ecommerce.spring.react.integration.stripe.StripeApiClient;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final StripeApiClient stripeApiClient;

    public CheckoutSessionResponse createCheckoutSession(CreateOrderRequest request) {
        Order order = orderService.createPendingOrder(request);

        try {
            User user = order.getUser();
            String customerId = user.getStripeCustomerId();

            if (customerId == null) {
                customerId = stripeApiClient.createCustomer(user, order);
                user.setStripeCustomerId(customerId);
                userRepository.save(user);
            } else {
                stripeApiClient.updateCustomerShipping(customerId, order);
            }

            StripeSessionResult session = stripeApiClient.createCheckoutSession(order, customerId);

            order.setStripeCheckoutSessionId(session.id());
            orderRepository.save(order);

            return new CheckoutSessionResponse(session.url());
        } catch (RuntimeException e) {
            orderService.cancelOrder(order);

            throw e;
        }
    }
}
