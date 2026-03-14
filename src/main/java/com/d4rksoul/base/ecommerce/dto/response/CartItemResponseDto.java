package com.d4rksoul.base.ecommerce.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CartItemResponseDto(
    Long id,

    int quantity,

    @JsonProperty("price_at_moment")
    BigDecimal priceAtMoment,

    int discount,

    ProductResponseDto product
) {}
