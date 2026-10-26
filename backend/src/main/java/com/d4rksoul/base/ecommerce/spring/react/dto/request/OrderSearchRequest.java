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
        page = (page == null) ? 0 : page;
        size = (size == null) ? 10 : size;
    }
}