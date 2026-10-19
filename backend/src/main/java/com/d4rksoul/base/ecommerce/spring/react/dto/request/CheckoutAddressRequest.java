package com.d4rksoul.base.ecommerce.spring.react.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CheckoutAddressRequest(
        @NotBlank(message = "Recipient name is required")
        String recipientName,

        @NotBlank(message = "Address line 1 is required")
        String addressLine1,

        String addressLine2,

        @NotBlank(message = "City is required")
        String city,

        String state,

        @NotBlank(message = "Postal code is required")
        String postalCode,

        @NotBlank(message = "Country is required")
        String country,

        @NotBlank(message = "Phone is required")
        String phone
) {}
