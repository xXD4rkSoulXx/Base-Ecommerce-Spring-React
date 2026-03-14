package com.d4rksoul.base.ecommerce.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProductResponseDto(
    Long id,

    String name,

    String description,

    @JsonProperty("image_url")
    String imageUrl,

    BigDecimal price,

    CategoryResponseDto category,

    DiscountResponseDto discount,

    List<ReviewResponseDto> reviews
) {}
