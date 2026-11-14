package com.d4rksoul.base.ecommerce.spring.react.dto.response;

import com.d4rksoul.base.ecommerce.spring.react.enums.ShipmentStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.util.List;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OrderShipmentResponse(
    Long id,

    String trackingNumber,

    ShipmentStatus status,

    List<OrderShipmentItemResponse> items,

    List<ShipmentTrackingEventResponse> events
) {}
