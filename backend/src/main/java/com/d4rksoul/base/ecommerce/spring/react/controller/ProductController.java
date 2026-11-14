package com.d4rksoul.base.ecommerce.spring.react.controller;

import com.d4rksoul.base.ecommerce.spring.react.dto.request.ProductSearchRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.ProductShopRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.ProductReviewsRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.ReviewRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.PageResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.ProductDetailsResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.ProductResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.ReviewEligibilityResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.ReviewResponse;
import com.d4rksoul.base.ecommerce.spring.react.enums.ReviewEligibility;
import com.d4rksoul.base.ecommerce.spring.react.service.ProductService;
import com.d4rksoul.base.ecommerce.spring.react.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ReviewService reviewService;

    @PostMapping("/{id}/review")
    public ResponseEntity<ReviewResponse> createReview(
        @PathVariable Long id, @Valid @RequestBody ReviewRequest request
    ) {
        ReviewResponse response = reviewService.createReview(id, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<PageResponse<ProductResponse>> getProducts(
        @ModelAttribute ProductShopRequest filter
    ) {
        PageResponse<ProductResponse> productsPage = productService.getProducts(filter);

        return ResponseEntity.ok(productsPage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDetailsResponse> getProductDetails(@PathVariable Long id) {
        ProductDetailsResponse response = productService.getProductDetails(id);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/related")
    public ResponseEntity<List<ProductResponse>> getRelatedProducts(@PathVariable Long id) {
        List<ProductResponse> related = productService.getRelatedProducts(id);

        return ResponseEntity.ok(related);
    }

    @GetMapping("/{id}/review/eligibility")
    public ResponseEntity<ReviewEligibilityResponse> getReviewEligibility(@PathVariable Long id) {
        ReviewEligibility eligibility = reviewService.getReviewEligibility(id);

        ReviewEligibilityResponse response = ReviewEligibilityResponse
            .builder()
            .status(eligibility)
            .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/reviews")
    public ResponseEntity<PageResponse<ReviewResponse>> getProductReviews(
        @PathVariable Long id, @ModelAttribute ProductReviewsRequest filter
    ) {
        PageResponse<ReviewResponse> reviews = reviewService.getProductReviews(id, filter);

        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/featured")
    public ResponseEntity<List<ProductResponse>> getFeaturedProducts() {
        List<ProductResponse> featured = productService.getFeaturedProducts();

        return ResponseEntity.ok(featured);
    }

    @GetMapping("/deals")
    public ResponseEntity<List<ProductResponse>> getDealProducts() {
        List<ProductResponse> deals = productService.getDealProducts();

        return ResponseEntity.ok(deals);
    }

    @GetMapping("/best-sellers")
    public ResponseEntity<List<ProductResponse>> getBestSellers() {
        List<ProductResponse> bestSellers = productService.getBestSellers();

        return ResponseEntity.ok(bestSellers);
    }

    @GetMapping("/top-rated")
    public ResponseEntity<List<ProductResponse>> getTopRatedProducts() {
        List<ProductResponse> topRated = productService.getTopRatedProducts();

        return ResponseEntity.ok(topRated);
    }

    @GetMapping("/search")
    public ResponseEntity<PageResponse<ProductResponse>> search(
        @ModelAttribute ProductSearchRequest search
    ) {
        PageResponse<ProductResponse> searchPage = productService.search(search);

        return ResponseEntity.ok(searchPage);
    }
}
