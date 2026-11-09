package com.d4rksoul.base.ecommerce.spring.react.dto.response.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AdminDashboardTopProductResponse(
        Long productId,

        String ref,

        String name,

        String cover,

        long unitsSold
) {}
