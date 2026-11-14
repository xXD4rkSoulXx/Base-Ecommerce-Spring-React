package com.d4rksoul.base.ecommerce.spring.react.dto.result;

import lombok.Builder;

@Builder
public record StripeRefundResult(
    String id,

    String status
) {}
