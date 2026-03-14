package com.d4rksoul.base.ecommerce.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.time.Instant;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ReviewResponseDto(
    Long id,

    Integer rating,

    String content,

    @JsonProperty("created_at")
    Instant createdAt
) {}
