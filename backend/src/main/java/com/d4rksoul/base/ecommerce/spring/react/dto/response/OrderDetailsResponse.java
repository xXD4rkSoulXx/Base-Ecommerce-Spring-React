package com.d4rksoul.base.ecommerce.spring.react.dto.response;

import com.d4rksoul.base.ecommerce.spring.react.enums.OrderStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nullable;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OrderDetailsResponse(
    Long id,

    OrderStatus status,

    Instant createdAt,

    String currency,

    @Nullable String discountCode,

    BigDecimal subtotal,

    BigDecimal discountAmount,

    BigDecimal shippingCost,

    BigDecimal taxAmount,

    BigDecimal totalAmount,

    String shippingCarrier,

    String shippingService,

    String recipientName,

    String addressLine1,

    @Nullable String addressLine2,

    String city,

    @Nullable String state,

    String postalCode,

    String country,

    String phone,

    List<OrderStatusEventResponse> events,

    List<OrderShipmentResponse> shipments,

    @Nullable Instant returnableUntil,

    List<OrderReturnResponse> returns,

    List<OrderItemResponse> items
) {}
