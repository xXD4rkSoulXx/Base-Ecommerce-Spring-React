package com.d4rksoul.base.ecommerce.spring.react.dto.response.admin;

import com.d4rksoul.base.ecommerce.spring.react.enums.FeaturedSlot;
import com.d4rksoul.base.ecommerce.spring.react.enums.ProductCategory;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nullable;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AdminProductSummaryResponse(
    Long id,

    String ref,

    String name,

    ProductCategory category,

    BigDecimal price,

    int discountPercentage,

    @Nullable FeaturedSlot featuredSlot,

    boolean active,

    @Nullable String cover,

    long totalStock,

    long skuCount,

    long lowStockSkus
) {}
