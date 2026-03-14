package com.d4rksoul.base.ecommerce.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record ReviewRequestDto(
    @NotBlank
    @Min(0)
    @Max(10)
    BigDecimal rating,

    String content,

    @JsonProperty("product_id")
    Long productId
) {}
