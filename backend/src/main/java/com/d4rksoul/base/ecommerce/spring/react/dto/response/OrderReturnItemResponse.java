package com.d4rksoul.base.ecommerce.spring.react.dto.response;

import lombok.Builder;

@Builder
public record OrderReturnItemResponse(
        Long orderItemId,

        int quantity
) {}
