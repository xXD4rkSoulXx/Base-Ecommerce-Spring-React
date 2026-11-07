package com.d4rksoul.base.ecommerce.spring.react.dto.request.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AdminAdjustStockRequest(
        @NotNull(message = "Adjustment is required")
        @Min(value = -100000, message = "Adjustment must be at least -100000")
        @Max(value = 100000, message = "Adjustment must be at most 100000")
        Integer delta
) {}
