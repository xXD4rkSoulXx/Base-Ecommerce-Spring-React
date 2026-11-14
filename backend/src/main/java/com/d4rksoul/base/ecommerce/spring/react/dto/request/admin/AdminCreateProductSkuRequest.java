package com.d4rksoul.base.ecommerce.spring.react.dto.request.admin;

import com.d4rksoul.base.ecommerce.spring.react.enums.ProductColor;
import com.d4rksoul.base.ecommerce.spring.react.enums.ProductSize;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AdminCreateProductSkuRequest(
    @NotNull(message = "Size is required")
    ProductSize size,

    @NotNull(message = "Color is required")
    ProductColor color,

    @NotNull(message = "Weight is required")
    @Min(value = 1, message = "Weight must be at least 1 g")
    @Max(value = 100000, message = "Weight must be at most 100000 g")
    Integer weightGrams,

    @NotNull(message = "Length is required")
    @Min(value = 1, message = "Length must be at least 1 cm")
    @Max(value = 1000, message = "Length must be at most 1000 cm")
    Integer lengthCm,

    @NotNull(message = "Width is required")
    @Min(value = 1, message = "Width must be at least 1 cm")
    @Max(value = 1000, message = "Width must be at most 1000 cm")
    Integer widthCm,

    @NotNull(message = "Height is required")
    @Min(value = 1, message = "Height must be at least 1 cm")
    @Max(value = 1000, message = "Height must be at most 1000 cm")
    Integer heightCm,

    @NotNull(message = "Discount is required")
    @Min(value = 0, message = "Discount must be between 0 and 100")
    @Max(value = 100, message = "Discount must be between 0 and 100")
    Integer discountPercentage
) {}
