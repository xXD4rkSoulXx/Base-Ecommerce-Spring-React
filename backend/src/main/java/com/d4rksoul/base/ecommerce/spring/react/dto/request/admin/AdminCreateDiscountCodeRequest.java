package com.d4rksoul.base.ecommerce.spring.react.dto.request.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.Locale;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AdminCreateDiscountCodeRequest(
    @NotBlank(message = "Code is required")
    @Pattern(
        regexp = "[A-Z0-9_-]{3,20}",
        message = "Code must have 3 to 20 characters: letters, numbers, - or _"
    )
    String code,

    @NotNull(message = "Discount is required")
    @Min(value = 1, message = "Discount must be between 1 and 100")
    @Max(value = 100, message = "Discount must be between 1 and 100")
    Integer discountPercentage
) {

    public AdminCreateDiscountCodeRequest {
        code = code == null ? null : code.trim().toUpperCase(Locale.ROOT);
    }
}
