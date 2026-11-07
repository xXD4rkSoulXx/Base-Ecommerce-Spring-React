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
        lowStock = Boolean.TRUE.equals(lowStock);
        page = (page == null) ? 0 : page;
        size = (size == null) ? 10 : size;
    }
}
