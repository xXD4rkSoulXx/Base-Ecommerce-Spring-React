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
        if (request == null) {
            return null;
        }

        return Review.builder()
                .rating(request.rating())
                .title(request.title())
                .description(request.description())
                .user(user)
                .product(product)
                .build();
    }

    public ReviewResponse toDto(Review review) {
        if (review == null) {
            return null;
        }

        return new ReviewResponse(
                review.getId(),
                review.getTitle(),
                review.getDescription(),
                review.getRating(),
                review.getUser().getFirstName() + " " + review.getUser().getLastName(),
                review.getCreatedAt()
        );
    }
}
