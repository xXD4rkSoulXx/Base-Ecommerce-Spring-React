package com.d4rksoul.base.ecommerce.spring.react.dto.result;

public record PackedParcelResult(
        String boxName,

        double weightKg,

        double lengthCm,

        double widthCm,

        double heightCm
) {}
