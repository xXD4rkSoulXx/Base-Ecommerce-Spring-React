package com.d4rksoul.base.ecommerce.spring.react.dto.response.admin;

import com.d4rksoul.base.ecommerce.spring.react.enums.OrderStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AdminOrderSummaryResponse(
        Long id,

        OrderStatus status,

        Instant createdAt,

        BigDecimal totalAmount,

        String currency,

        String customerEmail,

        String recipientName
) {}
