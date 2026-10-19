package com.d4rksoul.base.ecommerce.spring.react.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "shipping.packing")
@Getter
@Setter
public class ShippingPackingProperties {

    private List<Box> boxes;

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
