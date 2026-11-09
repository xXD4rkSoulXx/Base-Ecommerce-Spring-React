package com.d4rksoul.base.ecommerce.spring.react.dto.response.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AdminDashboardLowStockResponse(
        Long skuId,

        Long productId,

        String productName,

        String ref,

        String size,

        String color,

        int stock
) {}