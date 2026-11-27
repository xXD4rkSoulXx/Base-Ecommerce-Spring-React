package com.d4rksoul.base.ecommerce.spring.react.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Duration;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Constants {

    public static final int LOW_STOCK_THRESHOLD = 5;
    public static final int TOP_RATED_MIN_RATING = 3;
    public static final int HOME_PRODUCTS_LIMIT = 8;
    public static final int RELATED_PRODUCTS_LIMIT = 4;

    public static final int MAX_CART_ITEM_QUANTITY = 10;
    public static final int MAX_CART_LINES = 20;

    public static final Duration RETURN_WINDOW = Duration.ofDays(14);

    public static final Duration ACCESS_TOKEN_TTL = Duration.ofMinutes(15);
    public static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(30);

    public static final Duration PENDING_ORDER_TIMEOUT = Duration.ofMinutes(10);
    public static final Duration CHECKOUT_SESSION_DURATION = Duration.ofMinutes(31);

    public static final Duration SCHEDULER_ACTIVE_WINDOW = Duration.ofMinutes(5);

    public static final Duration SHIPPO_CONNECT_TIMEOUT = Duration.ofSeconds(5);
    public static final Duration SHIPPO_READ_TIMEOUT = Duration.ofSeconds(15);

    public static final Duration CLOUDINARY_CONNECT_TIMEOUT = Duration.ofSeconds(5);
    public static final Duration CLOUDINARY_READ_TIMEOUT = Duration.ofSeconds(30);

    public static final int IMAGE_MAX_WIDTH = 1200;
    public static final long IMAGE_MAX_SOURCE_PIXELS = 40_000_000L;
    public static final float IMAGE_JPEG_QUALITY = 0.85f;

    public static final int DASHBOARD_LIST_LIMIT = 5;
}
