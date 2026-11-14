package com.d4rksoul.base.ecommerce.spring.react.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "shipping.origin")
public record ShippingOriginProperties(
    String name,

    String addressLine1,

    String city,

    String state,

    String postalCode,

    String country,

    String phone
) {}
