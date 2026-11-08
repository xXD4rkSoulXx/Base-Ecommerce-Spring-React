package com.d4rksoul.base.ecommerce.spring.react.service.admin;

import com.d4rksoul.base.ecommerce.spring.react.dto.request.admin.AdminReviewSearchRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.PageResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminReviewResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.Review;
import com.d4rksoul.base.ecommerce.spring.react.exception.ReviewNotFoundException;
import com.d4rksoul.base.ecommerce.spring.react.mapper.PageMapper;
import com.d4rksoul.base.ecommerce.spring.react.mapper.admin.AdminReviewMapper;
import com.d4rksoul.base.ecommerce.spring.react.repository.ReviewRepository;
import com.d4rksoul.base.ecommerce.spring.react.specification.ReviewSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminReviewService {

    private final ReviewRepository reviewRepository;
    private final AdminReviewMapper adminReviewMapper;
    private final PageMapper pageMapper;

    @Transactional
    public void hideReview(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found with id: " + id));

        review.setHidden(true);
    }

    @Transactional
    public void showReview(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found with id: " + id));

        review.setHidden(false);
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminReviewResponse> getReviews(AdminReviewSearchRequest filter) {
        Specification<Review> specification = Specification
                .where(ReviewSpecification.containsQuery(filter.q()))
                .and(ReviewSpecification.hasHidden(filter.hidden()))
                .and(ReviewSpecification.orderByRecentDesc());

        Pageable pageable = PageRequest.of(filter.page(), filter.size());
        Page<AdminReviewResponse> responsePage = reviewRepository.findAll(specification, pageable)
                .map(adminReviewMapper::toDto);

        return pageMapper.toDto(responsePage);
    }
}
