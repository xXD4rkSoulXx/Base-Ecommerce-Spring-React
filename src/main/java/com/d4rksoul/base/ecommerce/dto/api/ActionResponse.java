package com.d4rksoul.base.ecommerce.dto.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.time.Instant;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ActionResponse(
    boolean success,

    String message,

    String action,

    @JsonProperty("resource_id")
    Long resourceId,

    Instant timestamp
) {
    public ActionResponse {
        if (timestamp == null) {
            timestamp = Instant.now();
        }
    }
}
