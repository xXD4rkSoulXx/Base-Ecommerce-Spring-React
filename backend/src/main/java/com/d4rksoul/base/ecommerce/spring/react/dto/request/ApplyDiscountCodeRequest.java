package com.d4rksoul.base.ecommerce.spring.react.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApplyDiscountCodeRequest(
    @NotBlank(message = "Code is required")
    @Size(max = 20, message = "Invalid discount code")
    String code
) {}
