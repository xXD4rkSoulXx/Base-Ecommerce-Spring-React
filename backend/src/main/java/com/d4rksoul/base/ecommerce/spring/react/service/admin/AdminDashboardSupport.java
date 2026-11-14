package com.d4rksoul.base.ecommerce.spring.react.service.admin;

import com.d4rksoul.base.ecommerce.spring.react.dto.request.admin.AdminDashboardPeriodRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AdminDashboardSupport {

    private static final ZoneId ZONE = ZoneOffset.UTC;

    public record Period(
        LocalDate start,

        LocalDate end,

        Instant from,

        Instant to,

        Instant previousFrom
    ) {}

    public static Period periodOf(AdminDashboardPeriodRequest period) {
        return new Period(
            LocalDate.now(ZONE).minusDays(period.days() - 1L),
            LocalDate.now(ZONE).plusDays(1),
            LocalDate.now(ZONE).minusDays(period.days() - 1L).atStartOfDay(ZONE).toInstant(),
            LocalDate.now(ZONE).plusDays(1).atStartOfDay(ZONE).toInstant(),
            LocalDate.now(ZONE).minusDays(period.days() - 1L).minusDays(period.days())
                .atStartOfDay(ZONE).toInstant()
        );
    }
}
