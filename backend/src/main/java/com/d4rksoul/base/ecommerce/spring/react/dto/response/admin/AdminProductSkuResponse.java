package com.d4rksoul.base.ecommerce.spring.react.dto.response.admin;

import com.d4rksoul.base.ecommerce.spring.react.enums.ProductColor;
import com.d4rksoul.base.ecommerce.spring.react.enums.ProductSize;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AdminProductSkuResponse(
    Long id,

    ProductSize size,

    ProductColor color,

    String cover,

    int stock,

    int weightGrams,

    int lengthCm,

    int widthCm,

    int heightCm,

    int discountPercentage,

    boolean active
) {}
