package com.d4rksoul.base.ecommerce.spring.react.simulation;

import com.d4rksoul.base.ecommerce.spring.react.dto.projection.ReturnedQuantityProjection;
import com.d4rksoul.base.ecommerce.spring.react.dto.result.StripeRefundResult;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderReturn;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderReturnItem;
import com.d4rksoul.base.ecommerce.spring.react.enums.OrderStatus;
import com.d4rksoul.base.ecommerce.spring.react.enums.ReturnStatus;
import com.d4rksoul.base.ecommerce.spring.react.exception.RefundFailedException;
import com.d4rksoul.base.ecommerce.spring.react.exception.StripeApiException;
import com.d4rksoul.base.ecommerce.spring.react.integration.stripe.StripeApiClient;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderReturnItemRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderReturnRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.ProductSkuRepository;
import com.d4rksoul.base.ecommerce.spring.react.service.OrderStatusEventService;
import com.d4rksoul.base.ecommerce.spring.react.service.admin.AdminReturnService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(prefix = "app.simulation", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class ReturnApprovalScheduler {

    private static final Set<String> FAILED_REFUND_STATUSES = Set.of("failed", "canceled");

    private final OrderReturnRepository orderReturnRepository;
    private final OrderReturnItemRepository orderReturnItemRepository;
    private final ProductSkuRepository productSkuRepository;
    private final OrderStatusEventService orderStatusEventService;
    private final AdminReturnService adminReturnService;
    private final StripeApiClient stripeApiClient;
    private final TransactionTemplate transactionTemplate;

    @Scheduled(fixedDelay = SimulationConstants.TICK_MS)
    public void processReturns() {
        Instant limit = Instant.now().minus(SimulationConstants.RETURN_APPROVAL_DELAY);

        List<OrderReturn> requestedReturns = orderReturnRepository
            .findByStatusAndCreatedAtBeforeAndManuallyManagedFalse(ReturnStatus.REQUESTED, limit);
        for (OrderReturn candidate : requestedReturns) {
            transactionTemplate.executeWithoutResult(_ -> orderReturnRepository
                .findByIdAndStatus(candidate.getId(), ReturnStatus.REQUESTED)
                .ifPresent(orderReturn -> {
                    orderReturn.setStatus(ReturnStatus.APPROVED);
                    orderReturn.setApprovedAt(Instant.now());
                }));
        }

        limit = Instant.now().minus(SimulationConstants.RETURN_RECEIVED_DELAY);

        List<OrderReturn> approvedReturns = orderReturnRepository
            .findByStatusAndApprovedAtBeforeAndManuallyManagedFalse(ReturnStatus.APPROVED, limit);
        for (OrderReturn candidate : approvedReturns) {
            transactionTemplate.executeWithoutResult(_ -> orderReturnRepository
                .findByIdAndStatus(candidate.getId(), ReturnStatus.APPROVED)
                .ifPresent(orderReturn -> {
                    orderReturn.setStatus(ReturnStatus.RECEIVED);
                    orderReturn.setReceivedAt(Instant.now());

                    List<OrderReturnItem> returnItems = orderReturnItemRepository
                        .findByOrderReturnId(orderReturn.getId());
                    for (OrderReturnItem item : returnItems) {
                        productSkuRepository.incrementStock(
                            item.getOrderItem().getProductSku().getId(), item.getQuantity()
                        );
                    }
                }));
        }

        limit = Instant.now().minus(SimulationConstants.RETURN_REFUND_DELAY);

        List<OrderReturn> receivedReturns = orderReturnRepository
            .findByStatusAndReceivedAtBeforeAndManuallyManagedFalse(ReturnStatus.RECEIVED, limit);
        for (OrderReturn candidate : receivedReturns) {
            try {
                refund(candidate.getId());
            } catch (RefundFailedException _) {/**/}
        }
    }

    private void refund(Long returnId) {
        transactionTemplate.executeWithoutResult(_ -> {
            OrderReturn orderReturn = orderReturnRepository
                .findByIdAndStatus(returnId, ReturnStatus.RECEIVED)
                .orElse(null);

            if (orderReturn == null) {
                return;
            }

            List<OrderReturnItem> returnItems = orderReturnItemRepository.findByOrderReturnId(returnId);

            Map<Long, Long> returnedByItemId = orderReturnItemRepository
                .sumReturnedQuantities(
                    orderReturn.getOrder().getId(), ReturnStatus.INACTIVE_STATUSES
                )
                .stream()
                .collect(Collectors.toMap(
                    ReturnedQuantityProjection::orderItemId,
                    ReturnedQuantityProjection::quantity
                ));

            boolean fullyReturned = orderReturn.getOrder().getOrderItems().stream()
                .allMatch(item ->
                    returnedByItemId.getOrDefault(item.getId(), 0L) >= item.getQuantity()
                );

            BigDecimal remaining = orderReturn.getOrder().getTotalAmount().subtract(
                orderReturnRepository.sumRefundAmountByOrderIdAndStatus(
                    orderReturn.getOrder().getId(), ReturnStatus.REFUNDED
                )
            );

            BigDecimal amount = adminReturnService.calculateRefund(
                orderReturn.getOrder(), returnItems, remaining, fullyReturned
            );

            StripeRefundResult refund;
            try {
                refund = stripeApiClient.createRefund(orderReturn.getOrder(), orderReturn, amount);
            } catch (StripeApiException _) {
                throw new RefundFailedException("The refund could not be completed. Please try again.");
            }

            if (FAILED_REFUND_STATUSES.contains(refund.status())) {
                throw new RefundFailedException("The refund could not be completed. Please try again.");
            }

            orderReturn.setStatus(ReturnStatus.REFUNDED);
            orderReturn.setRefundAmount(amount);
            orderReturn.setStripeRefundId(refund.id());
            orderReturn.setRefundedAt(Instant.now());

            orderStatusEventService.transition(
                orderReturn.getOrder().getId(),
                OrderStatus.RETURN_REQUESTED,
                fullyReturned ? OrderStatus.REFUNDED : OrderStatus.DELIVERED
            );
        });
    }
}
