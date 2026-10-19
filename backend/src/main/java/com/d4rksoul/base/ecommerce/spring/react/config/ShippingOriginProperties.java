package com.d4rksoul.base.ecommerce.spring.react.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "shipping.origin")
@Getter
@Setter
public class ShippingOriginProperties {

    private String name;

    private String addressLine1;

    private String city;

    private String state;

    private String postalCode;

    private String country;

    private String phone;
}
