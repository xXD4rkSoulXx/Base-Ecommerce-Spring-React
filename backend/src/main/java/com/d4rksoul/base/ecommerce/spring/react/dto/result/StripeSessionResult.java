package com.d4rksoul.base.ecommerce.spring.react.dto.result;

import lombok.Builder;

@Builder
public record StripeSessionResult(
    String id,

    String url
) {}
