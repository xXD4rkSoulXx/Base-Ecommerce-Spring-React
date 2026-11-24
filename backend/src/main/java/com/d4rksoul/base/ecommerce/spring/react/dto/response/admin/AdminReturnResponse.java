package com.d4rksoul.base.ecommerce.spring.react.dto.response.admin;

import com.d4rksoul.base.ecommerce.spring.react.enums.ReturnReason;
import com.d4rksoul.base.ecommerce.spring.react.enums.ReturnStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nullable;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AdminReturnResponse(
    Long id,

    Long orderId,

    String customerEmail,

    AdminReturnAddressResponse address,

    ReturnStatus status,

    ReturnReason reason,

    @Nullable String comment,

    @Nullable BigDecimal refundAmount,

    @Nullable String rejectionReason,

    Instant createdAt,

    @Nullable Instant refundedAt,

    List<AdminReturnItemResponse> items
) {}
