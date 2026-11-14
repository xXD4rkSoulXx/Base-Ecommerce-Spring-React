package com.d4rksoul.base.ecommerce.spring.react.dto.request.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AdminStockSearchRequest(
    String q,

    Boolean lowStock,

    Integer page,

    Integer size
) {

    public AdminStockSearchRequest {
        q = q == null || q.isBlank() ? null : q.trim();
        lowStock = Boolean.TRUE.equals(lowStock);
        page = (page == null) ? 0 : Math.max(page, 0);
        size = (size == null) ? 10 : Math.clamp(size, 1, 50);
    }
}
