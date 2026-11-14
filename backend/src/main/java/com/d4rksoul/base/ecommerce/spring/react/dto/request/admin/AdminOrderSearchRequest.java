package com.d4rksoul.base.ecommerce.spring.react.dto.request.admin;

import com.d4rksoul.base.ecommerce.spring.react.enums.OrderStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AdminOrderSearchRequest(
    String q,

    OrderStatus status,

    Boolean pending,

    Instant from,

    Instant to,

    Integer page,

    Integer size
) {

    public AdminOrderSearchRequest {
        q = q == null || q.isBlank() ? null : q.trim();
        pending = Boolean.TRUE.equals(pending);
        page = (page == null) ? 0 : Math.max(page, 0);
        size = (size == null) ? 10 : Math.clamp(size, 1, 50);
    }
}
