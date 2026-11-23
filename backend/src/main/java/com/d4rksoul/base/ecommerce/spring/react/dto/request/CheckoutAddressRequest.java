package com.d4rksoul.base.ecommerce.spring.react.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Locale;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CheckoutAddressRequest(
    @NotBlank(message = "Recipient name is required")
    @Size(max = 100, message = "Recipient name must be at most 100 characters")
    String recipientName,

    @NotBlank(message = "Address line 1 is required")
    @Size(max = 100, message = "Address line 1 must be at most 100 characters")
    String addressLine1,

    @Size(max = 100, message = "Address line 2 must be at most 100 characters")
    String addressLine2,

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must be at most 100 characters")
    String city,

    @Size(max = 100, message = "State must be at most 100 characters")
    String state,

    @NotBlank(message = "Postal code is required")
    @Size(max = 20, message = "Postal code must be at most 20 characters")
    String postalCode,

    @NotBlank(message = "Country is required")
    @Pattern(regexp = "[A-Z]{2}", message = "Country must be a 2-letter code, e.g. PT")
    String country,

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "\\+[1-9]\\d{6,14}", message = "Phone must be in international format, e.g. +351912345678")
    String phone
) {

    public CheckoutAddressRequest {
        recipientName = trim(recipientName);
        addressLine1 = trim(addressLine1);
        addressLine2 = trimToNull(addressLine2);
        city = trim(city);
        state = trimToNull(state);
        postalCode = trim(postalCode);
        country = country == null ? null : country.trim().toUpperCase(Locale.ROOT);
        phone = trim(phone);
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }

    private static String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
