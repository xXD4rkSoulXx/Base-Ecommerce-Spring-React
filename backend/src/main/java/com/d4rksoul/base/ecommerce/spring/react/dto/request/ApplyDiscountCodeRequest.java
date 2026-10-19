package com.d4rksoul.base.ecommerce.spring.react.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApplyDiscountCodeRequest(
        @NotBlank
        String code
) {}
