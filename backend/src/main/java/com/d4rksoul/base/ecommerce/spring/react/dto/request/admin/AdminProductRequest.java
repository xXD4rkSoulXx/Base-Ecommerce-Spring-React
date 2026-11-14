package com.d4rksoul.base.ecommerce.spring.react.dto.request.admin;

import com.d4rksoul.base.ecommerce.spring.react.enums.FeaturedSlot;
import com.d4rksoul.base.ecommerce.spring.react.enums.ProductCategory;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.Locale;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AdminProductRequest(
    @NotBlank(message = "Ref is required")
    @Size(max = 50, message = "Ref must be at most 50 characters")
    @Pattern(regexp = "[A-Z0-9-]+", message = "Ref can only contain letters, numbers and dashes")
    String ref,

    @NotBlank(message = "Name is required")
    @Size(max = 120, message = "Name must be at most 120 characters")
    String name,

    @NotBlank(message = "Description is required")
    @Size(max = 5000, message = "Description must be at most 5000 characters")
    String description,

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than zero")
    @DecimalMax(value = "99999.99", message = "Price must be at most 99999.99")
    @Digits(integer = 5, fraction = 2, message = "Price can have at most 2 decimal places")
    BigDecimal price,

    @NotNull(message = "Discount is required")
    @Min(value = 0, message = "Discount must be between 0 and 100")
    @Max(value = 100, message = "Discount must be between 0 and 100")
    Integer discountPercentage,

    @NotNull(message = "Category is required")
    ProductCategory category,

    FeaturedSlot featuredSlot
) {

    public AdminProductRequest {
        ref = (ref == null) ? null : ref.trim().toUpperCase(Locale.ROOT);
        name = (name == null) ? null : name.trim();
    }
}
