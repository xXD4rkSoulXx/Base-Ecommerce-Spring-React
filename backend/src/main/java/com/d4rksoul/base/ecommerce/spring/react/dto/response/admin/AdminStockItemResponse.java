package com.d4rksoul.base.ecommerce.spring.react.dto.response.admin;

import com.d4rksoul.base.ecommerce.spring.react.enums.ProductColor;
import com.d4rksoul.base.ecommerce.spring.react.enums.ProductSize;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nullable;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AdminStockItemResponse(
    Long skuId,

    Long productId,

    String productName,

    String productRef,

    ProductSize size,

    ProductColor color,

    @Nullable String cover,

    int stock,

    boolean lowStock
) {}
