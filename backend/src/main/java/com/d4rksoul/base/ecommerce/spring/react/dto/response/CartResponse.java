package com.d4rksoul.base.ecommerce.spring.react.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nullable;
import lombok.Builder;

import java.util.List;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CartResponse(
    List<CartItemResponse> items,

    @Nullable String discountCode,

    int discountPercentage
) {}
