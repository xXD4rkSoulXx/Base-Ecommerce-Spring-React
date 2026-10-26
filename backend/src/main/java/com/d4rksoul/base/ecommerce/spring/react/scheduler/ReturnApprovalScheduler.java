package com.d4rksoul.base.ecommerce.spring.react.scheduler;

import com.d4rksoul.base.ecommerce.spring.react.enums.ReturnStatus;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderReturnRepository;
import com.d4rksoul.base.ecommerce.spring.react.service.OrderReturnApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class ReturnApprovalScheduler {

    private static final Duration APPROVAL_DELAY = Duration.ofMinutes(10);

    private final OrderReturnRepository orderReturnRepository;
    private final OrderReturnApprovalService orderReturnApprovalService;

    @Scheduled(fixedDelay = 30_000)
    public void approveRequestedReturns() {
        Instant threshold = Instant.now().minus(APPROVAL_DELAY);

        orderReturnRepository.findByStatusAndCreatedAtBefore(ReturnStatus.REQUESTED, threshold)
                .forEach(orderReturn -> orderReturnApprovalService.approve(orderReturn.getId()));
    }
}
