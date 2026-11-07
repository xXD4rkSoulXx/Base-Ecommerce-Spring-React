package com.d4rksoul.base.ecommerce.spring.react.dto.response.admin;

import com.d4rksoul.base.ecommerce.spring.react.enums.FeaturedSlot;
import com.d4rksoul.base.ecommerce.spring.react.enums.ProductCategory;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AdminProductDetailsResponse(
        Long id,

        String ref,

        String name,

        String description,

        BigDecimal price,

        int discountPercentage,

        ProductCategory category,

        FeaturedSlot featuredSlot,

        boolean active
) {}