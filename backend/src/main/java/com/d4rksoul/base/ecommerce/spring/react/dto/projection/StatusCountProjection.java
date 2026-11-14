package com.d4rksoul.base.ecommerce.spring.react.dto.projection;

import com.d4rksoul.base.ecommerce.spring.react.enums.OrderStatus;

public record StatusCountProjection(
    OrderStatus status,

    Long count
) {}
