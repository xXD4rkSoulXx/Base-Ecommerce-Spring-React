package com.d4rksoul.base.ecommerce.spring.react.mapper;

import com.d4rksoul.base.ecommerce.spring.react.dto.request.ReviewRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.ReviewResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.Product;
import com.d4rksoul.base.ecommerce.spring.react.entity.Review;
import com.d4rksoul.base.ecommerce.spring.react.entity.User;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

    public Review toEntity(ReviewRequest request, User user, Product product) {
        return Review.builder()
            .rating(request.rating())
            .title(request.title())
            .description(request.description())
            .user(user)
            .product(product)
            .build();
    }

    public ReviewResponse toDto(Review review) {
        return ReviewResponse.builder()
            .id(review.getId())
            .title(review.getTitle())
            .description(review.getDescription())
            .rating(review.getRating())
            .userFullName(review.getUser().getFirstName() + " " + review.getUser().getLastName())
            .createdAt(review.getCreatedAt())
            .build();
    }
}
