package com.d4rksoul.base.ecommerce.spring.react.simulation;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Duration;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class SimulationConstants {

    static final long TICK_MS = 30_000;
    static final Duration ACTIVE_WINDOW = Duration.ofMinutes(5);

    static final Duration PAID_TO_PROCESSING_DELAY = Duration.ofMinutes(1);
    static final Duration PROCESSING_TO_SHIPPED_DELAY = Duration.ofMinutes(5);
    static final Duration SHIPPED_TO_DELIVERED_DELAY = Duration.ofMinutes(15);

    static final Duration SHIPMENT_MIN_WAIT = Duration.ofMinutes(5);
    static final Duration SHIPMENT_MAX_WAIT = Duration.ofMinutes(15);
    static final double SHIPMENT_ADVANCE_CHANCE = 0.1;

    static final Duration RETURN_APPROVAL_DELAY = Duration.ofMinutes(10);
    static final Duration RETURN_RECEIVED_DELAY = Duration.ofMinutes(10);
    static final Duration RETURN_REFUND_DELAY = Duration.ofMinutes(5);
}
