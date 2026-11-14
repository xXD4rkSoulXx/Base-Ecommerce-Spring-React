package com.d4rksoul.base.ecommerce.spring.react.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UpdateCartItemRequest(
    @Min(value = 1, message = "Quantity must be between 1 and 10")
    @Max(value = 10, message = "Quantity must be between 1 and 10")
    int quantity
) {}
