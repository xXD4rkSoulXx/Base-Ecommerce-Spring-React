package com.d4rksoul.base.ecommerce.spring.react.dto.response.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nullable;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AdminDashboardReviewsResponse(
    long newReviews,

    long previousNewReviews,

    @Nullable Double averageRating,

    @Nullable Double previousAverageRating
) {}
