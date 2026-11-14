package com.d4rksoul.base.ecommerce.spring.react.mapper.admin;

import com.d4rksoul.base.ecommerce.spring.react.dto.projection.ProductStockProjection;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.admin.AdminProductRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminProductDetailsResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminProductSummaryResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class AdminProductMapper {

    public Product toEntity(AdminProductRequest request) {
        return Product.builder()
            .ref(request.ref())
            .name(request.name())
            .description(request.description())
            .price(request.price())
            .discountPercentage(request.discountPercentage())
            .category(request.category())
            .featuredSlot(request.featuredSlot())
            .build();
    }

    public AdminProductSummaryResponse toSummaryDto(
        Product product, String cover, ProductStockProjection stock
    ) {
        return AdminProductSummaryResponse.builder()
            .id(product.getId())
            .ref(product.getRef())
            .name(product.getName())
            .category(product.getCategory())
            .price(product.getPrice())
            .discountPercentage(product.getDiscountPercentage())
            .featuredSlot(product.getFeaturedSlot())
            .active(product.isActive())
            .cover(cover)
            .totalStock(stock.totalStock())
            .skuCount(stock.skuCount())
            .lowStockSkus(stock.lowStockSkus())
            .build();
    }

    public AdminProductDetailsResponse toDetailsDto(Product product) {
        return AdminProductDetailsResponse.builder()
            .id(product.getId())
            .ref(product.getRef())
            .name(product.getName())
            .description(product.getDescription())
            .price(product.getPrice())
            .discountPercentage(product.getDiscountPercentage())
            .category(product.getCategory())
            .featuredSlot(product.getFeaturedSlot())
            .active(product.isActive())
            .build();
    }
}
