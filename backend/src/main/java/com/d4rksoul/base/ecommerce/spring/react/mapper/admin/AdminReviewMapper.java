package com.d4rksoul.base.ecommerce.spring.react.mapper.admin;

import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminReviewResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.Review;
import org.springframework.stereotype.Component;

@Component
public class AdminReviewMapper {

    public AdminReviewResponse toDto(Review review) {
        return AdminReviewResponse.builder()
                .id(review.getId())
                .productId(review.getProduct().getId())
                .productName(review.getProduct().getName())
                .productRef(review.getProduct().getRef())
                .customerName(review.getUser().getFirstName() + " " + review.getUser().getLastName())
                .customerEmail(review.getUser().getEmail())
                .rating(review.getRating())
                .title(review.getTitle())
                .description(review.getDescription())
                .hidden(review.isHidden())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
