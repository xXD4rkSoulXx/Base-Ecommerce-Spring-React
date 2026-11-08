package com.d4rksoul.base.ecommerce.spring.react.controller.admin;

import com.d4rksoul.base.ecommerce.spring.react.dto.request.admin.AdminReviewSearchRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.PageResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminReviewResponse;
import com.d4rksoul.base.ecommerce.spring.react.service.admin.AdminReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/reviews")
@RequiredArgsConstructor
public class AdminReviewController {

    private final AdminReviewService adminReviewService;

    @PostMapping("/{id}/hide")
    @PreAuthorize("hasAuthority('REVIEWS_WRITE')")
    public ResponseEntity<Void> hideReview(@PathVariable Long id) {
        adminReviewService.hideReview(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/show")
    @PreAuthorize("hasAuthority('REVIEWS_WRITE')")
    public ResponseEntity<Void> showReview(@PathVariable Long id) {
        adminReviewService.showReview(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAuthority('REVIEWS_READ')")
    public ResponseEntity<PageResponse<AdminReviewResponse>> getReviews(AdminReviewSearchRequest filter) {
        PageResponse<AdminReviewResponse> reviews = adminReviewService.getReviews(filter);

        return ResponseEntity.ok(reviews);
    }
}
