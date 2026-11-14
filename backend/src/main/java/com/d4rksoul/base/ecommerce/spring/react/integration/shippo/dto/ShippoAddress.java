package com.d4rksoul.base.ecommerce.spring.react.integration.shippo.dto;

import lombok.Builder;

@Builder
public record ShippoAddress(
    String name,

    String street1,

    String street2,

    String city,

    String state,

    String zip,

    String country,

    String phone,

    String email
) {}
