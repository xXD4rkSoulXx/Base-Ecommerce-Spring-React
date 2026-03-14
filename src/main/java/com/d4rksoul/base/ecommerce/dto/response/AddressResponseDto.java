package com.d4rksoul.base.ecommerce.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AddressResponseDto(
    Long id,

    String street,

    String city,

    String state,

    @JsonProperty("zip_code")
    String zipCode,

    String country
) {}
