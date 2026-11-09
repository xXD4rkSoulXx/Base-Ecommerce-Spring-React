package com.d4rksoul.base.ecommerce.spring.react.service.admin;

import com.d4rksoul.base.ecommerce.spring.react.dto.request.admin.AdminDashboardPeriodRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class DashboardSupport {

    static final int LIST_LIMIT = 5;
    static final int LOW_STOCK_THRESHOLD = 5;

    private static final ZoneId ZONE = ZoneId.systemDefault();

    static LocalDate startOf(AdminDashboardPeriodRequest period) {
        return LocalDate.now(ZONE).minusDays(period.days() - 1L);
    }

    static LocalDate endOf() {
        return LocalDate.now(ZONE).plusDays(1);
    }

    static Instant toInstant(LocalDate date) {
        return date.atStartOfDay(ZONE).toInstant();
    }
}
