package com.d4rksoul.base.ecommerce.spring.react.dto.response.admin;

import com.d4rksoul.base.ecommerce.spring.react.enums.ReturnReason;
import com.d4rksoul.base.ecommerce.spring.react.enums.ReturnStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
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

        String comment,

        BigDecimal refundAmount,

        String rejectionReason,

        Instant createdAt,

        Instant refundedAt,

        List<AdminReturnItemResponse> items
) {}
