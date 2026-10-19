package com.d4rksoul.base.ecommerce.spring.react.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.util.List;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProductSkuDetailsResponse(
        Long id,

        String size,

        String color,

        int stock,

        int discountPercentage,

        List<String> paths
) {}
