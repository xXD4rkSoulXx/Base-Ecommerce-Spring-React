package com.d4rksoul.base.ecommerce.spring.react.enums;

import lombok.Getter;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import static com.d4rksoul.base.ecommerce.spring.react.enums.ProductSize._15;
import static com.d4rksoul.base.ecommerce.spring.react.enums.ProductSize._46;
import static com.d4rksoul.base.ecommerce.spring.react.enums.ProductSize.ONE_SIZE;
import static com.d4rksoul.base.ecommerce.spring.react.enums.ProductSize.XS;
import static com.d4rksoul.base.ecommerce.spring.react.enums.ProductSize.XXL;

@Getter
public enum ProductCategory {

    OUTERWEAR(EnumSet.range(XS, XXL)),
    TOPS(EnumSet.range(XS, XXL)),
    BOTTOMS(EnumSet.range(XS, XXL)),
    FOOTWEAR(EnumSet.range(_15, _46)),
    UNDERWEAR(EnumSet.range(XS, XXL)),
    ACCESSORIES(EnumSet.of(ONE_SIZE));

    private final Set<ProductSize> allowedSizes;

    ProductCategory(Set<ProductSize> allowedSizes) {
        this.allowedSizes = Collections.unmodifiableSet(allowedSizes);
    }

    public boolean allowsSize(ProductSize size) {
        return allowedSizes.contains(size);
    }
}
