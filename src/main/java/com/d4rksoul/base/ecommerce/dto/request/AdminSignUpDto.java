package com.d4rksoul.base.ecommerce.dto.request;

import com.d4rksoul.base.ecommerce.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record AdminSignUpDto(
    @NotBlank
    String name,

    @NotBlank
    @Email
    String email,

    @NotBlank
    String password,

    @JsonProperty("phone_number")
    String phoneNumber,

    UserRole role,

    @JsonProperty("address_id")
    Long addressId
) {}
