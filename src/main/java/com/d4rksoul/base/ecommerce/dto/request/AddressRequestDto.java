package com.d4rksoul.base.ecommerce.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record AddressRequestDto(
    String street,

    String city,

    String state,

    @JsonProperty("zip_code")
    String zipCode,

    String country
) {}
