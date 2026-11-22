package com.d4rksoul.base.ecommerce.spring.react.enums;

import java.util.List;

public enum OrderStatus {

    PENDING,
    CANCELED,
    FAILED,
    PAID,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    RETURN_REQUESTED,
    REFUNDED,
    CANCELED_REFUNDED;

    public static final List<OrderStatus> HISTORY_STATUSES = List.of(
        PAID,
        PROCESSING,
        SHIPPED,
        DELIVERED,
        RETURN_REQUESTED,
        REFUNDED
    );

    public static final List<OrderStatus> VISIBLE_STATUSES = List.of(
        PAID,
        PROCESSING,
        SHIPPED,
        DELIVERED,
        RETURN_REQUESTED,
        REFUNDED,
        CANCELED_REFUNDED
    );

    public static final List<OrderStatus> FULFILLMENT_STATUSES = List.of(
        PAID,
        PROCESSING
    );

    public static final List<OrderStatus> DISCOUNT_UNUSED_STATUSES = List.of(
        PENDING,
        FAILED,
        CANCELED,
        REFUNDED,
        CANCELED_REFUNDED
    );
}
