package com.d4rksoul.base.ecommerce.spring.react.mapper;

import com.d4rksoul.base.ecommerce.spring.react.dto.response.ProductDetailsResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.ProductResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.ProductSkuDetailsResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductMapper {

    private final ProductSkuMapper productSkuMapper;

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

    public ProductDetailsResponse toDetailsDto(Product product, Double averageRating, Long totalReviews) {
        if (product == null) {
            return null;
        }

        List<ProductSkuDetailsResponse> productSkusDto = product.getProductSkus().stream()
                .map(productSkuMapper::toDetailsDto)
                .toList();

        return new ProductDetailsResponse(
                product.getId(),
                product.getRef(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getDiscountPercentage(),
                product.getCategory(),
                averageRating != null ? averageRating : 0.0,
                totalReviews != null ? totalReviews : 0L,
                productSkusDto
        );
    }
}
