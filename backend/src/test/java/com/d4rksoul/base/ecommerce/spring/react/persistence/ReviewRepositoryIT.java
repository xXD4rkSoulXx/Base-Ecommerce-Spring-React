package com.d4rksoul.base.ecommerce.spring.react.persistence;

import com.d4rksoul.base.ecommerce.spring.react.entity.Product;
import com.d4rksoul.base.ecommerce.spring.react.entity.Review;
import com.d4rksoul.base.ecommerce.spring.react.entity.User;
import com.d4rksoul.base.ecommerce.spring.react.repository.ReviewRepository;
import com.d4rksoul.base.ecommerce.spring.react.specification.ReviewSpecification;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.d4rksoul.base.ecommerce.spring.react.persistence.TestData.review;
import static org.assertj.core.api.Assertions.assertThat;

class ReviewRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Review addReview(User user, Product product, boolean hidden) {
        return persist(review(user, product, 5, hidden));
    }

    private Review addReview(Product product) {
        return addReview(addUser(), product, false);
    }

    private Review addReviewTitled(String title) {
        Review review = review(addUser(), addProduct("P-" + title), 5, false);

        review.setTitle(title);

        return persist(review);
    }

    private User addNamedUser(String email, String firstName, String lastName) {
        User user = TestData.user();

        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);

        return persist(user);
    }

    private Review addReviewOfUser(User user) {
        return addReview(user, addProduct("P-" + user.getEmail()), false);
    }

    private Product addNamedProduct(String ref, String name) {
        Product product = addProduct(ref);
        product.setName(name);

        return product;
    }

    private void setCreatedAt(Review review, Instant createdAt) {
        jdbcTemplate.update(
            "update reviews set created_at = ? where id = ?",
            Timestamp.from(createdAt),
            review.getId()
        );
    }

    private static List<Long> idsOf(List<Review> reviews) {
        return reviews.stream().map(Review::getId).toList();
    }

    private List<Long> search(Specification<Review> specification) {
        return idsOf(reviewRepository.findAll(specification));
    }

    @Test
    @DisplayName("exists by user and product: true only for that pair")
    void existsByUserIdAndProductId() {
        User author = addUser();
        User stranger = addUser();
        Product reviewed = addProduct("REVIEWED");
        Product other = addProduct("OTHER");
        addReview(author, reviewed, false);
        flushAndClear();

        assertThat(reviewRepository.existsByUserIdAndProductId(author.getId(), reviewed.getId())).isTrue();
        assertThat(reviewRepository.existsByUserIdAndProductId(author.getId(), other.getId())).isFalse();
        assertThat(reviewRepository.existsByUserIdAndProductId(stranger.getId(), reviewed.getId())).isFalse();
    }

    @Test
    @DisplayName("find by product: only visible reviews of that product, with the author loaded")
    void findByProductIdExcludesHiddenAndOtherProducts() {
        Product product = addProduct("A");
        Review visible = addReview(product);
        addReview(addUser(), product, true);
        addReview(addProduct("B"));
        flushAndClear();

        Page<Review> page = reviewRepository.findByProductId(
            product.getId(), PageRequest.of(0, 10)
        );

        assertThat(idsOf(page.getContent())).containsExactly(visible.getId());
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(Hibernate.isInitialized(page.getContent().getFirst().getUser())).isTrue();
    }

    @Test
    @DisplayName("find by product: pagination keeps counting every visible review")
    void findByProductIdPaginates() {
        Product product = addProduct("A");
        addReview(product);
        addReview(product);
        addReview(product);
        addReview(addUser(), product, true);
        flushAndClear();

        Page<Review> page = reviewRepository.findByProductId(
            product.getId(), PageRequest.of(0, 2)
        );

        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("spec search: matches the product name ignoring case")
    void searchMatchesProductName() {
        Review jacket = addReview(addNamedProduct("JKT-1", "Rain Jacket"));
        addReview(addNamedProduct("TEE-2", "Basic Tee"));
        flushAndClear();

        assertThat(search(ReviewSpecification.containsQuery(" RAIN ")))
            .containsExactly(jacket.getId());
    }

    @Test
    @DisplayName("spec search: matches the product ref")
    void searchMatchesProductRef() {
        addReview(addNamedProduct("JKT-1", "Rain Jacket"));
        Review tee = addReview(addNamedProduct("TEE-2", "Basic Tee"));
        flushAndClear();

        assertThat(search(ReviewSpecification.containsQuery("tee-2"))).containsExactly(tee.getId());
    }

    @Test
    @DisplayName("spec search: matches the email of the author")
    void searchMatchesUserEmail() {
        Review ana = addReviewOfUser(addNamedUser("ana@mail.com", "Ana", "Costa"));
        addReviewOfUser(addNamedUser("rui@mail.com", "Rui", "Lopes"));
        flushAndClear();

        assertThat(search(ReviewSpecification.containsQuery("ANA@"))).containsExactly(ana.getId());
    }

    @Test
    @DisplayName("spec search: matches the full name of the author, across first and last name")
    void searchMatchesUserFullName() {
        addReviewOfUser(addNamedUser("ana@mail.com", "Ana", "Costa"));
        Review rui = addReviewOfUser(addNamedUser("rui@mail.com", "Rui", "Lopes"));
        flushAndClear();

        assertThat(search(ReviewSpecification.containsQuery("rui lo"))).containsExactly(rui.getId());
    }

    @Test
    @DisplayName("spec search: matches the review title")
    void searchMatchesTitle() {
        Review good = addReviewTitled("Great fit");
        addReviewTitled("Too small");
        flushAndClear();

        assertThat(search(ReviewSpecification.containsQuery("great"))).containsExactly(good.getId());
    }

    @Test
    @DisplayName("spec search: a blank text does not filter")
    void blankSearchDoesNotFilter() {
        addReview(addProduct("A"));
        addReview(addProduct("B"));
        flushAndClear();

        assertThat(search(ReviewSpecification.containsQuery("  "))).hasSize(2);
        assertThat(search(ReviewSpecification.containsQuery(null))).hasSize(2);
    }

    @Test
    @DisplayName("spec hasHidden: filters by the flag and ignores null")
    void hasHidden() {
        Review visible = addReview(addProduct("A"));
        Review hidden = addReview(addUser(), addProduct("B"), true);
        flushAndClear();

        assertThat(search(ReviewSpecification.hasHidden(false))).containsExactly(visible.getId());
        assertThat(search(ReviewSpecification.hasHidden(true))).containsExactly(hidden.getId());
        assertThat(search(ReviewSpecification.hasHidden(null))).hasSize(2);
    }

    @Test
    @DisplayName("spec orderByRecentDesc: newest review first")
    void orderByRecentDesc() {
        Instant now = Instant.now();
        Review oldest = addReview(addProduct("A"));
        Review newest = addReview(addProduct("B"));
        Review middle = addReview(addProduct("C"));
        setCreatedAt(oldest, now.minus(3, ChronoUnit.DAYS));
        setCreatedAt(newest, now.minus(1, ChronoUnit.DAYS));
        setCreatedAt(middle, now.minus(2, ChronoUnit.DAYS));
        flushAndClear();

        List<Long> found = search(ReviewSpecification.orderByRecentDesc());

        assertThat(found).containsExactly(newest.getId(), middle.getId(), oldest.getId());
    }

    @Test
    @DisplayName("paged find all: loads user and product with each review and counts every match")
    void pagedFindAllLoadsUserAndProduct() {
        addReview(addProduct("A"));
        addReview(addProduct("B"));
        addReview(addProduct("C"));
        flushAndClear();

        Specification<Review> everything = ReviewSpecification.orderByRecentDesc();

        Page<Review> page = reviewRepository.findAll(everything, PageRequest.of(0, 2));

        Review first = page.getContent().getFirst();

        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(Hibernate.isInitialized(first.getUser())).isTrue();
        assertThat(Hibernate.isInitialized(first.getProduct())).isTrue();
    }
}
