package com.d4rksoul.base.ecommerce.spring.react.dto.projection;

public record RatingStatsProjection(
        Double averageRating,

        Long totalReviews
) {}
