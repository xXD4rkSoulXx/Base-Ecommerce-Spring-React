package com.d4rksoul.base.ecommerce.spring.react.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CreateOrderRequest(
    @NotNull(message = "Address is required")
    @Valid
    CheckoutAddressRequest address,

    @NotBlank(message = "Shipping rate is required")
    String rateId
) {}
