package com.d4rksoul.base.ecommerce.spring.react.scheduler;

import com.d4rksoul.base.ecommerce.spring.react.entity.Order;
import com.d4rksoul.base.ecommerce.spring.react.enums.OrderStatus;
import com.d4rksoul.base.ecommerce.spring.react.exception.StripeApiException;
import com.d4rksoul.base.ecommerce.spring.react.integration.stripe.StripeApiClient;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderExpirationScheduler {

    private final OrderRepository orderRepository;
    private final StripeApiClient stripeApiClient;

    @Scheduled(fixedDelay = 60_000)
    public void expireStaleOrders() {
        Instant limit = Instant.now().minus(10, ChronoUnit.MINUTES);

        List<Order> staleOrders = orderRepository
                .findByStatusAndStripeCheckoutSessionIdNotNullAndCreatedAtBefore(OrderStatus.PENDING, limit);
        for (Order order : staleOrders) {
            try {
                stripeApiClient.expireSession(order.getStripeCheckoutSessionId());
            } catch (StripeApiException _) {/**/}
        }
    }
}