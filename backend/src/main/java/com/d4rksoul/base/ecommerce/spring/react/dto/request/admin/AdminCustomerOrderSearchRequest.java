package com.d4rksoul.base.ecommerce.spring.react.dto.request.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AdminCustomerOrderSearchRequest(
        int page,

        int size
) {

    public AdminCustomerOrderSearchRequest {
        page = Math.max(page, 0);
        size = size <= 0 ? 10 : Math.min(size, 50);
    }
}
