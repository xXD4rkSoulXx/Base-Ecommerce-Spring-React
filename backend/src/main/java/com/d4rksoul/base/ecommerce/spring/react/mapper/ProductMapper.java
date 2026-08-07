package com.d4rksoul.base.ecommerce.spring.react.mapper;

import com.d4rksoul.base.ecommerce.spring.react.dto.response.ProductResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductResponse toDto(Product product, String cover) {
        if (product == null) {
            return null;
        }

        return ProductResponse.builder()
                .id(product.getId())
                .ref(product.getRef())
                .name(product.getName())
                .price(product.getPrice())
                .discountPercentage(product.getDiscountPercentage())
                .category(product.getCategory())
                .cover(cover)
                .build();
    }
}
