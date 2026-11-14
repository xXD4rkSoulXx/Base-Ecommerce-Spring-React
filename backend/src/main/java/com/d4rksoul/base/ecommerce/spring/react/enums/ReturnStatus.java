package com.d4rksoul.base.ecommerce.spring.react.enums;

import java.util.List;

public enum ReturnStatus {

    REQUESTED,
    APPROVED,
    REJECTED,
    RECEIVED,
    REFUNDED;

    public static final List<ReturnStatus> INACTIVE_STATUSES = List.of(REJECTED);

    public static final List<ReturnStatus> PENDING_STATUSES = List.of(
        REQUESTED,
        APPROVED,
        RECEIVED
    );
}
