package com.d4rksoul.base.ecommerce.spring.react.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ShippingRateResponse(
    String rateId,

    String carrier,

    String service,

    BigDecimal amount,

    String currency,

    String estimatedDays
) {}
