package com.d4rksoul.base.ecommerce.spring.react.mapper;

import com.d4rksoul.base.ecommerce.spring.react.dto.response.ProductSkuDetailsResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.ProductSku;
import com.d4rksoul.base.ecommerce.spring.react.entity.ProductSkuImage;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductSkuMapper {

    public ProductSkuDetailsResponse toDetailsDto(ProductSku productSku) {
        List<String> paths = productSku.getProductSkuImages().stream()
                .map(ProductSkuImage::getPath)
                .toList();

        return new ProductSkuDetailsResponse(
                productSku.getId(),
                productSku.getSize().name(),
                productSku.getColor().name(),
                productSku.getStock(),
                productSku.getDiscountPercentage(),
                paths
        );
    }
}
