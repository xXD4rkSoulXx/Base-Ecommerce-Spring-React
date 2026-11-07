package com.d4rksoul.base.ecommerce.spring.react.dto.request.admin;

import com.d4rksoul.base.ecommerce.spring.react.enums.ProductCategory;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AdminProductSearchRequest(
        String q,

        ProductCategory category,

        Boolean lowStock,

        Integer page,

        Integer size
) {

    public AdminProductSearchRequest {
        lowStock = Boolean.TRUE.equals(lowStock);
        page = (page == null) ? 0 : page;
        size = (size == null) ? 10 : size;
    }
}
