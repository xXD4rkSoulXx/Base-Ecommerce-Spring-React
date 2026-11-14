package com.d4rksoul.base.ecommerce.spring.react.dto.request.admin;

import com.d4rksoul.base.ecommerce.spring.react.enums.ReturnStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AdminReturnSearchRequest(
    ReturnStatus status,

    Boolean pending,

    Integer page,

    Integer size
) {

    public AdminReturnSearchRequest {
        pending = Boolean.TRUE.equals(pending);
        page = (page == null) ? 0 : Math.max(page, 0);
        size = (size == null) ? 10 : Math.clamp(size, 1, 50);
    }
}
