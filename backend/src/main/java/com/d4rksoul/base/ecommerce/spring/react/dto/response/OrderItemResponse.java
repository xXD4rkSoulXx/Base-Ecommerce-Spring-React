package com.d4rksoul.base.ecommerce.spring.react.dto.response;

import com.d4rksoul.base.ecommerce.spring.react.enums.ProductColor;
import com.d4rksoul.base.ecommerce.spring.react.enums.ProductSize;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OrderItemResponse(
    Long id,

    String ref,

    String productName,

    ProductColor color,

    ProductSize size,

    String imagePath,

    int quantity,

    BigDecimal unitPrice,

    BigDecimal lineTotal,

    int returnableQuantity
) {}
