package com.d4rksoul.base.ecommerce.spring.react.mapper;

import com.d4rksoul.base.ecommerce.spring.react.dto.response.ProductDetailsResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.ProductResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.Product;
import com.d4rksoul.base.ecommerce.spring.react.entity.ProductSku;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductMapper {

    private final ProductSkuMapper productSkuMapper;

    public ProductResponse toDto(Product product, String cover) {
        return ProductResponse.builder()
            .id(product.getId())
            .ref(product.getRef())
            .name(product.getName())
            .price(product.getPrice())
            .discountPercentage(product.getDiscountPercentage())
            .category(product.getCategory())
            .cover(cover)
            .featuredSlot(product.getFeaturedSlot())
            .build();
    }

    public ProductDetailsResponse toDetailsDto(
        Product product, Double averageRating, Long totalReviews
    ) {
        return ProductDetailsResponse.builder()
            .id(product.getId())
            .ref(product.getRef())
            .name(product.getName())
            .description(product.getDescription())
            .price(product.getPrice())
            .discountPercentage(product.getDiscountPercentage())
            .category(product.getCategory())
            .averageRating(averageRating != null ? averageRating : 0.0)
            .totalReviews(totalReviews != null ? totalReviews : 0L)
            .productSkus(
                product.getProductSkus().stream()
                    .filter(ProductSku::isActive)
                    .map(productSkuMapper::toDetailsDto)
                    .toList()
            )
            .build();
    }
}
