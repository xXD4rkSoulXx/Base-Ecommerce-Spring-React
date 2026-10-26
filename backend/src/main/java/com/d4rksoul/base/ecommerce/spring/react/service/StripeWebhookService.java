package com.d4rksoul.base.ecommerce.spring.react.service;

import com.d4rksoul.base.ecommerce.spring.react.dto.result.StripeCheckoutEventResult;
import com.d4rksoul.base.ecommerce.spring.react.entity.Order;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderItem;
import com.d4rksoul.base.ecommerce.spring.react.enums.OrderStatus;
import com.d4rksoul.base.ecommerce.spring.react.mapper.OrderStatusEventMapper;
import com.d4rksoul.base.ecommerce.spring.react.repository.CartItemRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.CartRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderStatusEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class StripeWebhookService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderStatusEventRepository orderStatusEventRepository;
    private final OrderStatusEventMapper orderStatusEventMapper;
    private final OrderService orderService;

    @Transactional
    public void handleEvent(StripeCheckoutEventResult event) {
        Order order = orderRepository.findByStripeCheckoutSessionId(event.sessionId()).orElse(null);
        if (order == null || order.getStatus() != OrderStatus.PENDING) {
            return;
        }

        switch (event.type()) {
            case "checkout.session.completed", "checkout.session.async_payment_succeeded" -> {
                if ("paid".equals(event.paymentStatus())) {
                    order.setStripePaymentIntentId(event.paymentIntentId());
                    order.setTaxAmount(event.taxAmount());
                    order.setTotalAmount(event.totalAmount());

                    Instant paidAt = Instant.now();
                    order.setPaidAt(paidAt);

                    cartRepository.findByUserId(order.getUser().getId()).ifPresent(cart -> {
                        for (OrderItem item : order.getOrderItems()) {
                            cartItemRepository.deleteByCartIdAndProductSkuId(
                                    cart.getId(), item.getProductSku().getId()
                            );
                        }

                        cart.setDiscountCode(null);
                    });

                    order.setStatus(OrderStatus.PAID);

                    orderStatusEventRepository.save(
                            orderStatusEventMapper.toEntity(order, OrderStatus.PAID, paidAt)
                    );
                }
            }
            case "checkout.session.expired" -> orderService.releaseReservation(order, OrderStatus.CANCELED);
            case "checkout.session.async_payment_failed" -> orderService.releaseReservation(order, OrderStatus.FAILED);
            default -> {/**/}
        }
    }
}
