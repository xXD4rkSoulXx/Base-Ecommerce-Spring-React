package com.d4rksoul.base.ecommerce.spring.react.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AddToCartRequest(
        @NotNull
        Long productSkuId,

        @Min(1)
        @Max(10)
        int quantity
) {}
