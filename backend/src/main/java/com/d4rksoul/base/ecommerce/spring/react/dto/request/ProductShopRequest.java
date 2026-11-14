package com.d4rksoul.base.ecommerce.spring.react.dto.request;

import com.d4rksoul.base.ecommerce.spring.react.enums.ProductCategory;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductShopRequest(
    ProductCategory category,

    Integer page,

    Integer size,

    String sortBy
) {

    public ProductShopRequest {
        page = (page == null) ? 0 : Math.max(page, 0);
        size = (size == null) ? 12 : Math.clamp(size, 1, 50);
        sortBy = (sortBy == null || sortBy.isBlank()) ? "recent" : sortBy;
    }
}
