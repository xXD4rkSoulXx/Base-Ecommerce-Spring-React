package com.d4rksoul.base.ecommerce.dto.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.Instant;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
    boolean success,

    String message,

    T data,

    Instant timestamp
) {
    public ApiResponse {
        if (timestamp ==  null) {
            timestamp = Instant.now();
        }
    }
}
