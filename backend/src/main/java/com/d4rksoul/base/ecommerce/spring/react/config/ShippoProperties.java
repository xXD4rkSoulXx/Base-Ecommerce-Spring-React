package com.d4rksoul.base.ecommerce.spring.react.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.shippo")
public record ShippoProperties(
    String apiKey,

    String baseUrl
) {}
