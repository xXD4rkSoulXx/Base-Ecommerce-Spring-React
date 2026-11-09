package com.d4rksoul.base.ecommerce.spring.react.dto.response.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AdminDashboardSummaryResponse(
        BigDecimal netRevenue,

        BigDecimal previousNetRevenue,

        BigDecimal refunded,

        BigDecimal previousRefunded,

        long orders,

        long previousOrders,

        BigDecimal averageOrderValue,

        BigDecimal previousAverageOrderValue
) {}
