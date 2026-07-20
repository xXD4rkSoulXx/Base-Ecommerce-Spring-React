package com.d4rksoul.base.ecommerce.spring.react.dto.response;

import lombok.Builder;

import java.time.Instant;

@Builder
public record ApiError(

        int status,

        String error,

        String message,

        Instant timestamp
) {}
