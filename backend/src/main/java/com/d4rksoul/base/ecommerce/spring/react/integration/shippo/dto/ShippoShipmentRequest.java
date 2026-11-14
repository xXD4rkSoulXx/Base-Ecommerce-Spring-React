package com.d4rksoul.base.ecommerce.spring.react.integration.shippo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@Builder
public record ShippoShipmentRequest(
    @JsonProperty("address_from")
    ShippoAddress addressFrom,

    @JsonProperty("address_to")
    ShippoAddress addressTo,

    List<ShippoParcel> parcels,

    boolean async
) {}
