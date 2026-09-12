package com.d4rksoul.base.ecommerce.spring.react.service;

import com.d4rksoul.base.ecommerce.spring.react.dto.request.ProductReviewsRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.ReviewRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.PageResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.ReviewResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.Product;
import com.d4rksoul.base.ecommerce.spring.react.entity.Review;
import com.d4rksoul.base.ecommerce.spring.react.entity.User;
import com.d4rksoul.base.ecommerce.spring.react.enums.OrderStatus;
import com.d4rksoul.base.ecommerce.spring.react.enums.ReviewEligibility;
import com.d4rksoul.base.ecommerce.spring.react.exception.ProductAlreadyReviewedException;
import com.d4rksoul.base.ecommerce.spring.react.exception.ProductNotFoundException;
import com.d4rksoul.base.ecommerce.spring.react.exception.ProductNotPurchasedException;
import com.d4rksoul.base.ecommerce.spring.react.exception.UserNotAuthenticatedException;
import com.d4rksoul.base.ecommerce.spring.react.mapper.ReviewMapper;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.ProductRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.ReviewRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ReviewMapper reviewMapper;

    @Transactional
    public ReviewResponse createReview(Long productId, ReviewRequest request) {
        User currentUser = getCurrentUser();
        Long userId = currentUser.getId();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " +  productId));

        boolean hasPurchased = orderRepository.hasUserPurchasedProduct(userId, productId, OrderStatus.DELIVERED);
        if (!hasPurchased) {
            throw new ProductNotPurchasedException("You can only review products that have been successfully delivered to you.");
        }

        boolean alreadyReviewed = reviewRepository.existsByUserIdAndProductId(userId, productId);
        if (alreadyReviewed) {
            throw new ProductAlreadyReviewedException("You have already reviewed this product. Only 1 review per product is allowed.");
        }

        Review review = reviewMapper.toEntity(request, currentUser, product);
        Review savedReview = reviewRepository.save(review);

        return reviewMapper.toDto(savedReview);
    }

    @Transactional(readOnly = true)
    public ReviewEligibility getReviewEligibility(Long productId) {
        User currentUser = getCurrentUser();
        Long userId = currentUser.getId();

        boolean hasPurchased = orderRepository.hasUserPurchasedProduct(userId, productId, OrderStatus.DELIVERED);
        if (!hasPurchased) {
            return ReviewEligibility.NOT_PURCHASED;
        }

        boolean alreadyReviewed = reviewRepository.existsByUserIdAndProductId(userId, productId);
        if (alreadyReviewed) {
            return ReviewEligibility.ALREADY_REVIEWED;
        }

        return ReviewEligibility.ELIGIBLE;
    }

    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> getProductReviews(Long productId, ProductReviewsRequest filter) {
        Pageable pageable = PageRequest.of(
                filter.page(),
                filter.size(),
                Sort.by(Sort.Direction.DESC, "createdAt", "id")
        );

        Page<Review> reviewPage = reviewRepository.findByProductId(productId, pageable);
        Page<ReviewResponse> responsePage = reviewPage.map(reviewMapper::toDto);

        return PageResponse.<ReviewResponse>builder()
                .content(responsePage.getContent())
                .page(responsePage.getNumber())
                .size(responsePage.getSize())
                .totalElements(responsePage.getTotalElements())
                .totalPages(responsePage.getTotalPages())
                .first(responsePage.isFirst())
                .last(responsePage.isLast())
                .build();
    }

    private User getCurrentUser() {
        Jwt jwt = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(auth -> (Jwt) auth.getPrincipal())
                .orElseThrow(() -> new UserNotAuthenticatedException("User is not authenticated"));

        return userRepository.findByEmail(jwt.getSubject())
                .orElseThrow(() -> new UserNotAuthenticatedException("User is not authenticated"));
    }
}
