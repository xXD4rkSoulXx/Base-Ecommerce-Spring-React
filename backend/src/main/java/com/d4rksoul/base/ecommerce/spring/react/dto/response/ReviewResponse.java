package com.d4rksoul.base.ecommerce.spring.react.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public record ReviewResponse(

        Long id,

        String title,

        String description,

        int rating,

        String userFullName,

        Instant createdAt
) {}
