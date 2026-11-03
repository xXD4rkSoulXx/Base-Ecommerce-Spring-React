package com.d4rksoul.base.ecommerce.spring.react.dto.request.admin;

import com.d4rksoul.base.ecommerce.spring.react.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(
        @NotNull(message = "Status is required")
        OrderStatus status
) {}
