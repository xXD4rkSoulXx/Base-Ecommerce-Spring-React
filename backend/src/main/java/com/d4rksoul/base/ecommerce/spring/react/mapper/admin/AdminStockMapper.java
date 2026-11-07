package com.d4rksoul.base.ecommerce.spring.react.mapper.admin;

import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminStockItemResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.ProductSku;
import org.springframework.stereotype.Component;

@Component
public class AdminStockMapper {

    public AdminStockItemResponse toDto(ProductSku sku, String cover) {
        return AdminStockItemResponse.builder()
                .skuId(sku.getId())
                .productId(sku.getProduct().getId())
                .productName(sku.getProduct().getName())
                .productRef(sku.getProduct().getRef())
                .size(sku.getSize())
                .color(sku.getColor())
                .cover(cover)
                .stock(sku.getStock())
                .lowStock(sku.getStock() <= 5)
                .build();
    }
}
