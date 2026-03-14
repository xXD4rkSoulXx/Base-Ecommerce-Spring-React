package com.d4rksoul.base.ecommerce.dto.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.time.Instant;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthResponse(
    boolean success,

    String message,

    String token,

    String role,

    @JsonProperty("expiration_time")
    Long expirationTime,

    Instant timestamp
) {
    public AuthResponse {
        if (timestamp == null) {
            timestamp = Instant.now();
        }
    }
}
