package com.d4rksoul.base.ecommerce.spring.react.service;

import com.d4rksoul.base.ecommerce.spring.react.dto.result.StripeRefundResult;
import com.d4rksoul.base.ecommerce.spring.react.entity.Order;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderItem;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderReturn;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderReturnItem;
import com.d4rksoul.base.ecommerce.spring.react.enums.OrderStatus;
import com.d4rksoul.base.ecommerce.spring.react.enums.ReturnStatus;
import com.d4rksoul.base.ecommerce.spring.react.exception.StripeApiException;
import com.d4rksoul.base.ecommerce.spring.react.integration.stripe.StripeApiClient;
import com.d4rksoul.base.ecommerce.spring.react.mapper.OrderItemMapper;
import com.d4rksoul.base.ecommerce.spring.react.mapper.OrderReturnItemMapper;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderReturnItemRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderReturnRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.ProductSkuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrderReturnApprovalService {

    private static final Set<String> FAILED_REFUND_STATUSES = Set.of("failed", "canceled");

    private final OrderReturnRepository orderReturnRepository;
    private final OrderReturnItemRepository orderReturnItemRepository;
    private final ProductSkuRepository productSkuRepository;
    private final OrderStatusEventService orderStatusEventService;
    private final OrderItemMapper orderItemMapper;
    private final OrderReturnItemMapper orderReturnItemMapper;
    private final StripeApiClient stripeApiClient;

    @Transactional
    public void approve(Long returnId) {
        OrderReturn orderReturn = orderReturnRepository
                .findByIdAndStatus(returnId, ReturnStatus.REQUESTED)
                .orElse(null);

        if (orderReturn == null) {
            return;
        }

        Order order = orderReturn.getOrder();
        List<OrderReturnItem> returnItems = orderReturnItemRepository.findByOrderReturnId(returnId);
        BigDecimal amount = calculateRefund(order, returnItems);

        Optional<StripeRefundResult> refund = refund(order, orderReturn, amount);

        if (refund.isEmpty()) {
            orderReturn.setStatus(ReturnStatus.FAILED);
            orderStatusEventService.transition(order.getId(), OrderStatus.RETURN_REQUESTED, OrderStatus.DELIVERED);

            return;
        }

        orderReturn.setStatus(ReturnStatus.REFUNDED);
        orderReturn.setRefundAmount(amount);
        orderReturn.setStripeRefundId(refund.get().id());
        orderReturn.setRefundedAt(Instant.now());

        for (OrderReturnItem item : returnItems) {
            productSkuRepository.incrementStock(item.getOrderItem().getProductSku().getId(), item.getQuantity());
        }

        OrderStatus next = isFullyReturned(order) ? OrderStatus.REFUNDED : OrderStatus.DELIVERED;

        orderStatusEventService.transition(order.getId(), OrderStatus.RETURN_REQUESTED, next);
    }

    private Optional<StripeRefundResult> refund(Order order, OrderReturn orderReturn, BigDecimal amount) {
        try {
            StripeRefundResult refund = stripeApiClient.createRefund(order, orderReturn, amount);

            return FAILED_REFUND_STATUSES.contains(refund.status()) ? Optional.empty() : Optional.of(refund);
        } catch (StripeApiException _) {
            return Optional.empty();
        }
    }

    private BigDecimal calculateRefund(Order order, List<OrderReturnItem> returnItems) {
        BigDecimal remaining = order.getTotalAmount().subtract(
                orderReturnRepository.sumRefundAmountByOrderIdAndStatus(order.getId(), ReturnStatus.REFUNDED)
        );

        if (isFullyReturned(order)) {
            return remaining;
        }

        BigDecimal discountFactor = BigDecimal.valueOf(100L - order.getDiscountPercentage()).movePointLeft(2);

        BigDecimal itemsNet = order.getOrderItems().stream()
                .map(item -> lineValue(item, item.getQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .multiply(discountFactor);

        BigDecimal returnedNet = returnItems.stream()
                .map(item -> lineValue(item.getOrderItem(), item.getQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .multiply(discountFactor);

        double totalWeight = order.getOrderItems().stream()
                .mapToDouble(item -> weight(item, item.getQuantity()))
                .sum();
        double returnedWeight = returnItems.stream()
                .mapToDouble(item -> weight(item.getOrderItem(), item.getQuantity()))
                .sum();

        BigDecimal shippingShare = order.getShippingCost()
                .multiply(BigDecimal.valueOf(returnedWeight))
                .divide(BigDecimal.valueOf(totalWeight), 2, RoundingMode.HALF_UP);

        return returnedNet.add(shippingShare)
                .multiply(order.getTotalAmount())
                .divide(itemsNet.add(order.getShippingCost()), 2, RoundingMode.HALF_UP)
                .min(remaining);
    }

    private boolean isFullyReturned(Order order) {
        Map<Long, Long> returnedByItemId = orderReturnItemMapper.toReturnedQuantities(
                orderReturnItemRepository.sumReturnedQuantities(order.getId(), ReturnStatus.FAILED)
        );

        return order.getOrderItems().stream()
                .allMatch(item -> returnedByItemId.getOrDefault(item.getId(), 0L) >= item.getQuantity());
    }

    private BigDecimal lineValue(OrderItem item, int quantity) {
        return orderItemMapper.toUnitPrice(item).multiply(BigDecimal.valueOf(quantity));
    }

    private double weight(OrderItem item, int quantity) {
        return item.getProductSku().getWeight() * (double) quantity;
    }
}
