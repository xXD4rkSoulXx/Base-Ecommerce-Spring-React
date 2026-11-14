package com.d4rksoul.base.ecommerce.spring.react.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "shipping.packing")
public record ShippingPackingProperties(
    List<Box> boxes
) {

    public record Box(
        String name,

        double maxWeightKg,

        double lengthCm,

        double widthCm,

        double heightCm
    ) {

        public double volumeCm3() {
            return lengthCm * widthCm * heightCm;
        }
    }
}
