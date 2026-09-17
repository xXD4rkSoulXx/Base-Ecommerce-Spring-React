package com.d4rksoul.base.ecommerce.spring.react.dto.response;

import com.d4rksoul.base.ecommerce.spring.react.enums.ProductCategory;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProductDetailsResponse(

        Long id,

        String ref,

        String name,

        String description,

        BigDecimal price,

        int discountPercentage,

        ProductCategory category,

        Double averageRating,

        Long totalReviews,

        List<ProductSkuDetailsResponse> productSkus
) {}
