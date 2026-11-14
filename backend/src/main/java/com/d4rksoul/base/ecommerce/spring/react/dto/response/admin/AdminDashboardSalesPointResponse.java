package com.d4rksoul.base.ecommerce.spring.react.dto.response.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AdminDashboardSalesPointResponse(
    LocalDate date,

    BigDecimal sales,

    BigDecimal refunds,

    long orders
) {}
