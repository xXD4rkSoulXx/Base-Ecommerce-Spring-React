package com.d4rksoul.base.ecommerce.spring.react.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductReviewsRequest(
        Integer page,

        Integer size
) {

    public ProductReviewsRequest {
        page = (page == null) ? 0 : page;
        size = (size == null) ? 5 : size;
    }
}
