package com.d4rksoul.base.ecommerce.spring.react.dto.result;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record PeriodTotalsResult(
    BigDecimal grossRevenue,

    BigDecimal refunded,

    long orders
) {}
