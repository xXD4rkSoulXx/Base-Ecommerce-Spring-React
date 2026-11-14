package com.d4rksoul.base.ecommerce.spring.react.dto.request.admin;

import com.d4rksoul.base.ecommerce.spring.react.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Locale;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AdminAssignRoleRequest(
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email,

    @NotNull(message = "Role is required")
    UserRole role
) {

    public AdminAssignRoleRequest {
        email = (email == null) ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
