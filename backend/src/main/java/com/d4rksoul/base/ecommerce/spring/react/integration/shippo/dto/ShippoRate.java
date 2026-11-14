package com.d4rksoul.base.ecommerce.spring.react.integration.shippo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ShippoRate(
    @JsonProperty("object_id")
    String objectId,

    String amount,

    String currency,

    String provider,

    ShippoServiceLevel servicelevel,

    @JsonProperty("estimated_days")
    Integer estimatedDays
) {}
