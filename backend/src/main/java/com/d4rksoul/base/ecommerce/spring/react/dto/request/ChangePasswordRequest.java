package com.d4rksoul.base.ecommerce.spring.react.dto.request;

import com.d4rksoul.base.ecommerce.spring.react.validation.StrongPassword;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ChangePasswordRequest(
        @NotBlank(message = "Current password is required")
        String currentPassword,

        @StrongPassword
        String newPassword
) {}
