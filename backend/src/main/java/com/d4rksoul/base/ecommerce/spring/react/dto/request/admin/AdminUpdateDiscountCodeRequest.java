package com.d4rksoul.base.ecommerce.spring.react.dto.request.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AdminUpdateDiscountCodeRequest(
    @NotNull(message = "Discount is required")
    @Min(value = 1, message = "Discount must be between 1 and 100")
    @Max(value = 100, message = "Discount must be between 1 and 100")
    Integer discountPercentage
) {}
