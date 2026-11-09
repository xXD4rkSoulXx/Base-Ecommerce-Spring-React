package com.d4rksoul.base.ecommerce.spring.react.dto.result;

import java.math.BigDecimal;

public record PeriodTotalsResult(
        BigDecimal grossRevenue,

        BigDecimal refunded,

        long orders
) {}
