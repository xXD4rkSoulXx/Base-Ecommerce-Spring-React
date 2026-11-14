package com.d4rksoul.base.ecommerce.spring.react.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.stripe")
public record StripeProperties(
    String secretKey,

    String webhookSecret,

    String currency
) {}
