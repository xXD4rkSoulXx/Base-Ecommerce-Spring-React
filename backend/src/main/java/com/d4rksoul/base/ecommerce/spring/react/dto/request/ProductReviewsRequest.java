package com.d4rksoul.base.ecommerce.spring.react.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductReviewsRequest(
    Integer page,

    Integer size
) {

    public ProductReviewsRequest {
        page = (page == null) ? 0 : Math.max(page, 0);
        size = (size == null) ? 5 : Math.clamp(size, 1, 50);
    }
}
