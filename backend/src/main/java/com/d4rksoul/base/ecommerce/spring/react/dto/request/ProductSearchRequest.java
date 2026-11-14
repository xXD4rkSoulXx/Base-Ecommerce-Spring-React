package com.d4rksoul.base.ecommerce.spring.react.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductSearchRequest(
    String q,

    Integer page,

    Integer size
) {

    public ProductSearchRequest {
        q = q == null || q.isBlank() ? null : q.trim();
        page = (page == null) ? 0 : Math.max(page, 0);
        size = (size == null) ? 12 : Math.clamp(size, 1, 50);
    }
}
