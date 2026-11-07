package com.d4rksoul.base.ecommerce.spring.react.dto.response.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.Instant;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AdminCustomerResponse(
        Long id,

        String email,

        String firstName,

        String lastName,

        Instant createdAt,

        long orders,

        Instant lastOrderAt
) {}
