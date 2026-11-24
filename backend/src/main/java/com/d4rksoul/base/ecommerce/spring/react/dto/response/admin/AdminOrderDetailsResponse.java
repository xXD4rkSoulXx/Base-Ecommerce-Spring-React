package com.d4rksoul.base.ecommerce.spring.react.dto.response.admin;

import com.d4rksoul.base.ecommerce.spring.react.dto.response.OrderDetailsResponse;
import com.d4rksoul.base.ecommerce.spring.react.enums.OrderStatus;
import com.d4rksoul.base.ecommerce.spring.react.enums.ShipmentStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nullable;
import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AdminOrderDetailsResponse(
    String customerEmail,

    String customerName,

    @Nullable OrderStatus nextStatus,

    Map<Long, List<ShipmentStatus>> shipmentActions,

    OrderDetailsResponse order
) {}
