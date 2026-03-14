package com.d4rksoul.base.ecommerce.dto.response;

import com.d4rksoul.base.ecommerce.enums.OrderStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OrderResponseDto(
    Long id,

    @JsonProperty("total_price")
    BigDecimal totalPrice,

    OrderStatus status,

    List<OrderItemResponseDto> items,

    List<PaymentResponseDto> payments,

    @JsonProperty("created_at")
    Instant createdAt
) {}
