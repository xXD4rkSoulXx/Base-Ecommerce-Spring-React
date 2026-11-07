package com.d4rksoul.base.ecommerce.spring.react.dto.projection;

public record ProductStockProjection(
        Long productId,

        Long totalStock,

        Long skuCount,

        Long lowStockSkus
) {}
