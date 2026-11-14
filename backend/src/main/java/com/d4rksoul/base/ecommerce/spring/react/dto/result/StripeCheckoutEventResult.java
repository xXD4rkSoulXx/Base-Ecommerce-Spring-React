package com.d4rksoul.base.ecommerce.spring.react.dto.result;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record StripeCheckoutEventResult(
    String type,

    String sessionId,

    String paymentStatus,

    String paymentIntentId,

    BigDecimal taxAmount,

    BigDecimal totalAmount
) {}
