package com.d4rksoul.base.ecommerce.spring.react.dto.request.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AdminDashboardPeriodRequest(Integer days) {

    public AdminDashboardPeriodRequest {
        days = (days == null) ? 30 : Math.clamp(days, 1, 365);
    }
}
