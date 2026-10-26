package com.d4rksoul.base.ecommerce.spring.react.dto.result;

import java.math.BigDecimal;

public record StripeCheckoutEventResult(
        String type,

        String sessionId,

        String paymentStatus,

        String paymentIntentId,

        BigDecimal taxAmount,

        BigDecimal totalAmount
) {}
