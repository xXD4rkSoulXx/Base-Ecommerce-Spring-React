package com.d4rksoul.base.ecommerce.dto.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.time.Instant;
import java.util.Map;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
    int status,

    String error,

    String message,

    @JsonProperty("error_code")
    String errorCode,

    String path,

    Map<String, String> errors,

    Instant timestamp
) {
    public ApiError {
        if (timestamp == null) {
            timestamp = Instant.now();
        }
    }
}
