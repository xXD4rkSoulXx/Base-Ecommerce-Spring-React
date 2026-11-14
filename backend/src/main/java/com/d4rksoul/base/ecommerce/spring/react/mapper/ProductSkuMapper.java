package com.d4rksoul.base.ecommerce.spring.react.mapper;

import com.d4rksoul.base.ecommerce.spring.react.dto.response.ProductSkuDetailsResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.ProductSku;
import com.d4rksoul.base.ecommerce.spring.react.entity.ProductSkuImage;
import org.springframework.stereotype.Component;

@Component
public class ProductSkuMapper {

    public ProductSkuDetailsResponse toDetailsDto(ProductSku productSku) {
        return ProductSkuDetailsResponse.builder()
            .id(productSku.getId())
            .size(productSku.getSize())
            .color(productSku.getColor())
            .stock(productSku.getStock())
            .discountPercentage(productSku.getDiscountPercentage())
            .paths(
                productSku.getProductSkuImages()
                    .stream()
                    .map(ProductSkuImage::getPath)
                    .toList()
            )
            .build();
    }
}
