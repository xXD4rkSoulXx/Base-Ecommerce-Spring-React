package com.d4rksoul.base.ecommerce.spring.react.integration.shippo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record ShippoParcel(
    double length,

    double width,

    double height,

    @JsonProperty("distance_unit")
    String distanceUnit,

    double weight,

    @JsonProperty("mass_unit")
    String massUnit
) {}
