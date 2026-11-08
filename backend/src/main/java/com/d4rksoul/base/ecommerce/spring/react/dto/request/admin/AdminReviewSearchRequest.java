package com.d4rksoul.base.ecommerce.spring.react.dto.request.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AdminReviewSearchRequest(
        String q,

        Boolean hidden,

        int page,

        int size
) {

    public AdminReviewSearchRequest {
        q = q == null || q.isBlank() ? null : q.trim();
        page = Math.max(page, 0);
        size = size <= 0 ? 10 : Math.min(size, 50);
    }
}
