package com.d4rksoul.base.ecommerce.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductRequestDto(
    @NotBlank
    String name,

    String description,

    @JsonProperty("image_url")
    String imageUrl,

    @PositiveOrZero
    BigDecimal price,

    @JsonProperty("category_id")
    Long categoryId,

    @JsonProperty("discount_id")
    Long discountId
) {}
