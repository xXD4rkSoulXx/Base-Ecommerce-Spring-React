package com.d4rksoul.base.ecommerce.spring.react.dto.response.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AdminReturnAddressResponse(
        String recipientName,

        String addressLine1,

        String addressLine2,

        String city,

        String state,

        String postalCode,

        String country,

        String phone
) {}
