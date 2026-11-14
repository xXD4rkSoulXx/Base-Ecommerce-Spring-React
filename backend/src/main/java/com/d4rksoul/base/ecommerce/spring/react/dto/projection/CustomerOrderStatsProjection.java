package com.d4rksoul.base.ecommerce.spring.react.dto.projection;

import java.time.Instant;

public record CustomerOrderStatsProjection(
    Long userId,

    Long orders,

    Instant lastOrderAt
) {}
