package com.d4rksoul.base.ecommerce.spring.react.integration.shippo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ShippoTransactionResponse(
        @JsonProperty("object_id")
        String objectId,

        String status,

        @JsonProperty("tracking_number")
        String trackingNumber
) {}
