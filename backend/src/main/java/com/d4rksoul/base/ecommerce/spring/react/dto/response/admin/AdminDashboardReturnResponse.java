package com.d4rksoul.base.ecommerce.spring.react.dto.response.admin;

import com.d4rksoul.base.ecommerce.spring.react.enums.ReturnStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.Instant;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AdminDashboardReturnResponse(
        Long id,

        Long orderId,

        String customerName,

        ReturnStatus status,

        Instant createdAt
) {}
