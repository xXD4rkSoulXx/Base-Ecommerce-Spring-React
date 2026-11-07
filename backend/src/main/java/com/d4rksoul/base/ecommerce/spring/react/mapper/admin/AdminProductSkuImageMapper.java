package com.d4rksoul.base.ecommerce.spring.react.mapper.admin;

import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminProductSkuImageResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.ProductSku;
import com.d4rksoul.base.ecommerce.spring.react.entity.ProductSkuImage;
import org.springframework.stereotype.Component;

@Component
public class AdminProductSkuImageMapper {

    public ProductSkuImage toEntity(ProductSku sku, String path, boolean cover) {
        return ProductSkuImage.builder()
                .productSku(sku)
                .path(path)
                .cover(cover)
                .build();
    }

    public AdminProductSkuImageResponse toDto(ProductSkuImage image) {
        return AdminProductSkuImageResponse.builder()
                .id(image.getId())
                .path(image.getPath())
                .cover(image.isCover())
                .build();
    }
}
