package com.d4rksoul.base.ecommerce.spring.react.dto.result;

public record PackingItemResult(
        double weightKg,

        double lengthCm,

        double widthCm,

        double heightCm
) {

    public double volumeCm3() {
        return lengthCm * widthCm * heightCm;
    }
}
