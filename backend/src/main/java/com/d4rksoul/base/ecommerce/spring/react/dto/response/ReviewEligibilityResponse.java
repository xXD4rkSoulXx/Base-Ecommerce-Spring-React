package com.d4rksoul.base.ecommerce.spring.react.dto.response;

import com.d4rksoul.base.ecommerce.spring.react.enums.ReviewEligibility;
import lombok.Builder;

@Builder
public record ReviewEligibilityResponse(

        ReviewEligibility status
) {}
