package com.d4rksoul.base.ecommerce.spring.react.dto.response;

import com.d4rksoul.base.ecommerce.spring.react.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserResponse(
        String email,

        String firstName,

        String lastName,

        UserRole role
) {}
