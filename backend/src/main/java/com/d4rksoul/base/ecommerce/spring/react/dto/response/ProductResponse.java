package com.d4rksoul.base.ecommerce.spring.react.dto.response;

import com.d4rksoul.base.ecommerce.spring.react.enums.ProductCategory;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public record ProductResponse(

        Long id,

        String ref,

        String name,

        BigDecimal price,

        int discountPercentage,

        ProductCategory category,

        String cover
) {}
