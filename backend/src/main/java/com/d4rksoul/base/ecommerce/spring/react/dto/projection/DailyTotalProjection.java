package com.d4rksoul.base.ecommerce.spring.react.dto.projection;

import java.math.BigDecimal;

public record DailyTotalProjection(
    Integer year,

    Integer month,

    Integer day,

    BigDecimal total,

    Long count
) {}
