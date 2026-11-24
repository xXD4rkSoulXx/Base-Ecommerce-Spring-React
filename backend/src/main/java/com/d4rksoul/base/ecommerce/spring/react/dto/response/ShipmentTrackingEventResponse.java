package com.d4rksoul.base.ecommerce.spring.react.dto.response;

import com.d4rksoul.base.ecommerce.spring.react.enums.ShipmentStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nullable;
import lombok.Builder;

import java.time.Instant;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ShipmentTrackingEventResponse(
    ShipmentStatus status,

    @Nullable String description,

    @Nullable String location,

    Instant occurredAt
) {}
