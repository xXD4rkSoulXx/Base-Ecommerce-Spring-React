package com.d4rksoul.base.ecommerce.dto.response;

import com.d4rksoul.base.ecommerce.enums.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.time.Instant;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PaymentResponseDto(
    Long id,

    String method,

    PaymentStatus status,

    @JsonProperty("created_at")
    Instant createdAt
) {}
