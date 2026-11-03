package com.d4rksoul.base.ecommerce.spring.react.enums;

import java.util.List;

public enum OrderStatus {

    PENDING, CANCELED, FAILED, PAID, PROCESSING, SHIPPED, RETURN_REQUESTED, DELIVERED, REFUNDED;

    public static final List<OrderStatus> HISTORY_STATUSES = List.of(
            PAID, PROCESSING, SHIPPED, DELIVERED, RETURN_REQUESTED, REFUNDED
    );
    public static final List<OrderStatus> FULFILLMENT_STATUSES = List.of(PROCESSING, PAID);
}
