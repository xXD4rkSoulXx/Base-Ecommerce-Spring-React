package com.d4rksoul.base.ecommerce.spring.react.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.Instant;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ReviewResponse(
        Long id,

        String title,

        String description,

        int rating,

        String userFullName,

        Instant createdAt
) {}
