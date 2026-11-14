package com.d4rksoul.base.ecommerce.spring.react.dto.projection;

public record ProductStockProjection(
    Long productId,

    Long totalStock,

    Long skuCount,

    Long lowStockSkus
) {

    public static ProductStockProjection empty(Long productId) {
        return new ProductStockProjection(productId, 0L, 0L, 0L);
    }
}
