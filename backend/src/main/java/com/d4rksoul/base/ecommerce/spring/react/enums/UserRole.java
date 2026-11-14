package com.d4rksoul.base.ecommerce.spring.react.enums;

import lombok.Getter;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

@Getter
public enum UserRole {

    CUSTOMER(EnumSet.noneOf(Permission.class)),
    SUPPORT(EnumSet.of(
        Permission.DASHBOARD_READ,
        Permission.PRODUCTS_READ,
        Permission.ORDERS_READ,
        Permission.RETURNS_READ,
        Permission.RETURNS_WRITE,
        Permission.DISCOUNTS_READ,
        Permission.CUSTOMERS_READ,
        Permission.REVIEWS_READ,
        Permission.REVIEWS_WRITE
    )),
    FULFILLMENT(EnumSet.of(
        Permission.DASHBOARD_READ,
        Permission.PRODUCTS_READ,
        Permission.STOCK_READ,
        Permission.STOCK_WRITE,
        Permission.ORDERS_READ,
        Permission.ORDERS_WRITE,
        Permission.RETURNS_READ
    )),
    MERCHANDISER(EnumSet.of(
        Permission.DASHBOARD_READ,
        Permission.PRODUCTS_READ,
        Permission.PRODUCTS_WRITE,
        Permission.STOCK_READ,
        Permission.DISCOUNTS_READ,
        Permission.DISCOUNTS_WRITE,
        Permission.REVIEWS_READ,
        Permission.REVIEWS_WRITE
    )),
    MANAGER(EnumSet.complementOf(EnumSet.of(Permission.TEAM_WRITE))),
    ADMIN(EnumSet.allOf(Permission.class));

    private final Set<Permission> permissions;

    UserRole(Set<Permission> permissions) {
        this.permissions = Collections.unmodifiableSet(permissions);
    }
}
