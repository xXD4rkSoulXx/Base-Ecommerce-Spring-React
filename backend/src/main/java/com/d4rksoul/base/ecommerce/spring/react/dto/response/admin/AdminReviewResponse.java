package com.d4rksoul.base.ecommerce.spring.react.dto.response.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.Instant;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AdminReviewResponse(
    Long id,

    Long productId,

    String productName,

    String productRef,

    String customerName,

    String customerEmail,

    int rating,

    String title,

    String description,

    boolean hidden,

    Instant createdAt
) {}
