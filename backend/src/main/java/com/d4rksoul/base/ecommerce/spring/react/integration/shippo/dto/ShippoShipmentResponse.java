package com.d4rksoul.base.ecommerce.spring.react.integration.shippo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ShippoShipmentResponse(
        @JsonProperty("object_id")
        String objectId,

        List<ShippoRate> rates
) {}
