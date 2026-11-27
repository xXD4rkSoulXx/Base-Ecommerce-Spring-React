package com.d4rksoul.base.ecommerce.spring.react.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.cloudinary")
public record CloudinaryProperties(
    String baseUrl,

    String cloudName,

    String apiKey,

    String apiSecret,

    String folder
) {}
