package com.d4rksoul.base.ecommerce.spring.react.dto.result;

import java.time.Instant;
import java.util.Map;

public record ReturnEligibilityResult(
        Instant returnableUntil,

        Map<Long, Long> returnableByItemId
) {}
