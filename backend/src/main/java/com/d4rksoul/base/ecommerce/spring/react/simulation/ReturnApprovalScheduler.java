package com.d4rksoul.base.ecommerce.spring.react.simulation;

import com.d4rksoul.base.ecommerce.spring.react.enums.ReturnStatus;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderReturnRepository;
import com.d4rksoul.base.ecommerce.spring.react.service.OrderReturnApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
@ConditionalOnProperty(prefix = "app.simulation", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class ReturnApprovalScheduler {

    private static final Duration APPROVAL_DELAY = Duration.ofMinutes(10);
    private static final Duration RECEIPT_DELAY = Duration.ofMinutes(10);
    private static final Duration REFUND_DELAY = Duration.ofMinutes(5);

    private final OrderReturnRepository orderReturnRepository;
    private final OrderReturnApprovalService orderReturnApprovalService;

    @Scheduled(fixedDelay = 30_000)
    public void processReturns() {
        approveRequested();
        receiveApproved();
        refundReceived();
    }

    private void approveRequested() {
        Instant limit = Instant.now().minus(APPROVAL_DELAY);

        orderReturnRepository
                .findByStatusAndCreatedAtBeforeAndManuallyManagedFalse(ReturnStatus.REQUESTED, limit)
                .forEach(orderReturn -> orderReturnApprovalService.approve(orderReturn.getId()));
    }

    private void receiveApproved() {
        Instant limit = Instant.now().minus(RECEIPT_DELAY);

        orderReturnRepository
                .findByStatusAndApprovedAtBeforeAndManuallyManagedFalse(ReturnStatus.APPROVED, limit)
                .forEach(orderReturn -> orderReturnApprovalService.receive(orderReturn.getId()));
    }

    private void refundReceived() {
        Instant limit = Instant.now().minus(REFUND_DELAY);

        orderReturnRepository
                .findByStatusAndReceivedAtBeforeAndManuallyManagedFalse(ReturnStatus.RECEIVED, limit)
                .forEach(orderReturn -> orderReturnApprovalService.refund(orderReturn.getId()));
    }
}
