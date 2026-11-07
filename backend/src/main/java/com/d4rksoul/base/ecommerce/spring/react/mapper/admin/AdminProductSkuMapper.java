package com.d4rksoul.base.ecommerce.spring.react.mapper.admin;

import com.d4rksoul.base.ecommerce.spring.react.dto.request.admin.AdminCreateProductSkuRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.admin.AdminUpdateProductSkuRequest;
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
                .weight(request.weight())
                .length(request.length())
                .width(request.width())
                .height(request.height())
                .discountPercentage(request.discountPercentage())
                .build();
    }

    public void updateEntity(ProductSku sku, AdminUpdateProductSkuRequest request) {
        sku.setWeight(request.weight());
        sku.setLength(request.length());
        sku.setWidth(request.width());
        sku.setHeight(request.height());
        sku.setDiscountPercentage(request.discountPercentage());
    }

    public AdminProductSkuResponse toDto(ProductSku sku, String cover) {
        return AdminProductSkuResponse.builder()
                .id(sku.getId())
                .size(sku.getSize())
                .color(sku.getColor())
                .cover(cover)
                .stock(sku.getStock())
                .weight(sku.getWeight())
                .length(sku.getLength())
                .width(sku.getWidth())
                .height(sku.getHeight())
                .discountPercentage(sku.getDiscountPercentage())
                .active(sku.isActive())
                .build();
    }
}
