package com.d4rksoul.base.ecommerce.spring.react.dto.request.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AdminDiscountCodeSearchRequest(
    String q,

    Boolean active,

    int page,

    int size
) {

    public AdminDiscountCodeSearchRequest {
        q = q == null || q.isBlank() ? null : q.trim();
        page = Math.max(page, 0);
        size = Math.clamp(size, 1, 50);
    }
}
