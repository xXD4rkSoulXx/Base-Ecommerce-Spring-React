package com.d4rksoul.base.ecommerce.spring.react.dto.request;

import com.d4rksoul.base.ecommerce.spring.react.enums.ShipmentStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateShipmentStatusRequest(
        @NotNull(message = "Status is required")
        ShipmentStatus status
) {}
