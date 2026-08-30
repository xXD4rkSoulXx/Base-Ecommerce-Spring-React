package com.d4rksoul.base.ecommerce.spring.react.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public record ProductSkuDetailsResponse(

        Long id,

        String size,

        String color,

        int stock,

        int discountPercentage,

        List<String> paths
) {}
