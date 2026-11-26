package com.d4rksoul.base.ecommerce.spring.react.service;

import com.d4rksoul.base.ecommerce.spring.react.dto.request.ProductReviewsRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.ReviewRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.ReviewResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.Product;
import com.d4rksoul.base.ecommerce.spring.react.entity.Review;
import com.d4rksoul.base.ecommerce.spring.react.entity.User;
import com.d4rksoul.base.ecommerce.spring.react.enums.OrderStatus;
import com.d4rksoul.base.ecommerce.spring.react.enums.ReviewEligibility;
import com.d4rksoul.base.ecommerce.spring.react.exception.ProductAlreadyReviewedException;
import com.d4rksoul.base.ecommerce.spring.react.exception.ProductNotFoundException;
import com.d4rksoul.base.ecommerce.spring.react.exception.ProductNotPurchasedException;
import com.d4rksoul.base.ecommerce.spring.react.mapper.PageMapper;
import com.d4rksoul.base.ecommerce.spring.react.mapper.ReviewMapper;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.ProductRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.ReviewRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    private static final long USER_ID = 1L;
    private static final long PRODUCT_ID = 10L;

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserService userService;
    @Mock
    private ReviewMapper reviewMapper;
    @Mock
    private PageMapper pageMapper;

    @Captor
    private ArgumentCaptor<Pageable> pageable;

    @InjectMocks
    private ReviewService service;

    private final User user = User.builder()
        .id(USER_ID)
        .build();

    private final Product product = Product.builder()
        .id(PRODUCT_ID)
        .build();

    private final ReviewRequest request =
        new ReviewRequest(5, "  Great  ", "  Very good jacket  ");

    @Test
    @DisplayName("create review: rejects a product that does not exist")
    void createReviewRejectsUnknownProduct() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createReview(PRODUCT_ID, request))
            .isInstanceOf(ProductNotFoundException.class);

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("create review: rejects a user who has not received the product")
    void createReviewRejectsUserWhoDidNotBuyTheProduct() {
        givenProduct();

        when(orderRepository.existsUserPurchasedProduct(USER_ID, PRODUCT_ID, OrderStatus.DELIVERED))
            .thenReturn(false);

        assertThatThrownBy(() -> service.createReview(PRODUCT_ID, request))
            .isInstanceOf(ProductNotPurchasedException.class);

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("create review: rejects a second review of the same product by the same user")
    void createReviewRejectsSecondReview() {
        givenProduct();
        givenPurchased(true);

        when(reviewRepository.existsByUserIdAndProductId(USER_ID, PRODUCT_ID)).thenReturn(true);

        assertThatThrownBy(() -> service.createReview(PRODUCT_ID, request))
            .isInstanceOf(ProductAlreadyReviewedException.class);

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("create review: saves the review of a user who received the product")
    void createReviewSavesReview() {
        Review review = mock(Review.class);
        Review saved = mock(Review.class);
        ReviewResponse response = mock(ReviewResponse.class);

        givenProduct();
        givenPurchased(true);

        when(reviewRepository.existsByUserIdAndProductId(USER_ID, PRODUCT_ID))
            .thenReturn(false);
        when(reviewMapper.toEntity(request, user, product)).thenReturn(review);
        when(reviewRepository.save(review)).thenReturn(saved);
        when(reviewMapper.toDto(saved)).thenReturn(response);

        ReviewResponse result = service.createReview(PRODUCT_ID, request);

        assertThat(result).isSameAs(response);
    }

    @Test
    @DisplayName("eligibility: not purchased when the user did not receive the product")
    void eligibilityNotPurchased() {
        when(userService.getCurrentUser()).thenReturn(user);

        givenPurchased(false);

        assertThat(service.getReviewEligibility(PRODUCT_ID))
            .isEqualTo(ReviewEligibility.NOT_PURCHASED);

        verify(reviewRepository, never()).existsByUserIdAndProductId(any(), any());
    }

    @Test
    @DisplayName("eligibility: already reviewed when the user has a review for the product")
    void eligibilityAlreadyReviewed() {
        when(userService.getCurrentUser()).thenReturn(user);

        givenPurchased(true);

        when(reviewRepository.existsByUserIdAndProductId(USER_ID, PRODUCT_ID))
            .thenReturn(true);

        assertThat(service.getReviewEligibility(PRODUCT_ID))
            .isEqualTo(ReviewEligibility.ALREADY_REVIEWED);
    }

    @Test
    @DisplayName("eligibility: eligible when the user received the product and did not review it yet")
    void eligibilityEligible() {
        when(userService.getCurrentUser()).thenReturn(user);

        givenPurchased(true);

        when(reviewRepository.existsByUserIdAndProductId(USER_ID, PRODUCT_ID))
            .thenReturn(false);

        assertThat(service.getReviewEligibility(PRODUCT_ID)).isEqualTo(ReviewEligibility.ELIGIBLE);
    }

    @Test
    @DisplayName("product reviews: asks for the requested page, newest first")
    void productReviewsAreRequestedNewestFirst() {
        when(reviewRepository.findByProductId(eqProduct(), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of()));

        service.getProductReviews(PRODUCT_ID, new ProductReviewsRequest(2, 10));

        verify(reviewRepository).findByProductId(eqProduct(), pageable.capture());

        assertThat(pageable.getValue().getPageNumber()).isEqualTo(2);
        assertThat(pageable.getValue().getPageSize()).isEqualTo(10);
        assertThat(pageable.getValue().getSort())
            .isEqualTo(Sort.by(Sort.Direction.DESC, "createdAt", "id"));
    }

    private void givenProduct() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
    }

    private void givenPurchased(boolean purchased) {
        when(orderRepository.existsUserPurchasedProduct(USER_ID, PRODUCT_ID, OrderStatus.DELIVERED))
            .thenReturn(purchased);
    }

    private static Long eqProduct() {
        return org.mockito.ArgumentMatchers.eq(PRODUCT_ID);
    }
}
