package com.d4rksoul.base.ecommerce.spring.react.dto.response;

import com.d4rksoul.base.ecommerce.spring.react.enums.ProductColor;
import com.d4rksoul.base.ecommerce.spring.react.enums.ProductSize;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CartItemResponse(
    Long id,

    Long productSkuId,

    Long productId,

    String productName,

    String productRef,

    ProductSize size,

    ProductColor color,

    String cover,

    BigDecimal price,

    int discountPercentage,

    int stock,

    int quantity,

    boolean available
) {}
