package com.d4rksoul.base.ecommerce.spring.react.dto.result;

import lombok.Builder;

@Builder
public record PackingItemResult(
    Long productSkuId,

    double weightKg,

    double lengthCm,

    double widthCm,

    double heightCm
) {

    public double volumeCm3() {
        return lengthCm * widthCm * heightCm;
    }
}
