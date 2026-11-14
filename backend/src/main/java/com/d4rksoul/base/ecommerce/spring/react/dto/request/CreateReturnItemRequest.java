package com.d4rksoul.base.ecommerce.spring.react.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateReturnItemRequest(
    @NotNull(message = "Order item is required.")
    Long orderItemId,

    @Positive(message = "Quantity must be at least 1.")
    int quantity
) {}
