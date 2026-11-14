package com.d4rksoul.base.ecommerce.spring.react.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderSearchRequest(
    String q,

    Instant from,

    Instant to,

    Integer page,

    Integer size
) {

    public OrderSearchRequest {
        q = q == null || q.isBlank() ? null : q.trim();
        page = (page == null) ? 0 : Math.max(page, 0);
        size = (size == null) ? 10 : Math.clamp(size, 1, 50);
    }
}
