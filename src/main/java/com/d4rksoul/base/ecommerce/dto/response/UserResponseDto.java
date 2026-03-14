package com.d4rksoul.base.ecommerce.dto.response;

import com.d4rksoul.base.ecommerce.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserResponseDto(
        Long id,

        String name,

        String email,

        @JsonProperty("phone_number")
        String phoneNumber,

        UserRole role,

        AddressResponseDto address,

        CartResponseDto cart,

        List<OrderResponseDto> orders,

        List<ReviewResponseDto> reviews
) {}
