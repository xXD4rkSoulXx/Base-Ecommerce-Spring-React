package com.d4rksoul.base.ecommerce.spring.react.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductSearchRequest(

        String q,

        Integer page,

        Integer size
) {

    public ProductSearchRequest {
        page = (page == null) ? 0 : page;
        size = (size == null) ? 12 : size;
    }
}
