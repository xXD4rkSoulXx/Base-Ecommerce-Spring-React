package com.d4rksoul.base.ecommerce.spring.react.mapper.admin;

import com.d4rksoul.base.ecommerce.spring.react.dto.request.admin.AdminCreateProductSkuRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminProductSkuResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.Product;
import com.d4rksoul.base.ecommerce.spring.react.entity.ProductSku;
import org.springframework.stereotype.Component;

@Component
public class AdminProductSkuMapper {

    public ProductSku toEntity(Product product, AdminCreateProductSkuRequest request) {
        return ProductSku.builder()
            .product(product)
            .size(request.size())
            .color(request.color())
            .stock(0)
            .weightGrams(request.weightGrams())
            .lengthCm(request.lengthCm())
            .widthCm(request.widthCm())
            .heightCm(request.heightCm())
            .discountPercentage(request.discountPercentage())
            .build();
    }

    public AdminProductSkuResponse toDto(ProductSku sku, String cover) {
        return AdminProductSkuResponse.builder()
            .id(sku.getId())
            .size(sku.getSize())
            .color(sku.getColor())
            .cover(cover)
            .stock(sku.getStock())
            .weightGrams(sku.getWeightGrams())
            .lengthCm(sku.getLengthCm())
            .widthCm(sku.getWidthCm())
            .heightCm(sku.getHeightCm())
            .discountPercentage(sku.getDiscountPercentage())
            .active(sku.isActive())
            .build();
    }
}
