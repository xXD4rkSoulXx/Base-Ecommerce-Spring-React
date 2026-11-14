package com.d4rksoul.base.ecommerce.spring.react.dto.projection;

import com.d4rksoul.base.ecommerce.spring.react.enums.ProductCategory;

public record CategoryUnitsProjection(
    ProductCategory category,

    Long unitsSold
) {}
