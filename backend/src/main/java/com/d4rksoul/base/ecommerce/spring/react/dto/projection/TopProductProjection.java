package com.d4rksoul.base.ecommerce.spring.react.dto.projection;

public record TopProductProjection(
        Long productId,

        String ref,

        String name,

        Long unitsSold
) {}
