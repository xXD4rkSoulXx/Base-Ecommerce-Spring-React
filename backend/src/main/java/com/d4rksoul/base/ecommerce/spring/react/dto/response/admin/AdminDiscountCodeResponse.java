package com.d4rksoul.base.ecommerce.spring.react.dto.response.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.Instant;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AdminDiscountCodeResponse(
    Long id,

    String code,

    int discountPercentage,

    boolean active,

    long orders,

    Instant createdAt
) {}
