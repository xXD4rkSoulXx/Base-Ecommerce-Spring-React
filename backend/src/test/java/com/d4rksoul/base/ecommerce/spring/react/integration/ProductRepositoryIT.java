package com.d4rksoul.base.ecommerce.spring.react.integration;

import com.d4rksoul.base.ecommerce.spring.react.dto.projection.RatingStatsProjection;
import com.d4rksoul.base.ecommerce.spring.react.entity.Order;
import com.d4rksoul.base.ecommerce.spring.react.entity.Product;
import com.d4rksoul.base.ecommerce.spring.react.entity.User;
import com.d4rksoul.base.ecommerce.spring.react.enums.FeaturedSlot;
import com.d4rksoul.base.ecommerce.spring.react.enums.OrderStatus;
import com.d4rksoul.base.ecommerce.spring.react.enums.ProductCategory;
import com.d4rksoul.base.ecommerce.spring.react.enums.ProductSize;
import com.d4rksoul.base.ecommerce.spring.react.repository.ProductRepository;
import com.d4rksoul.base.ecommerce.spring.react.specification.ProductSpecification;
import jakarta.persistence.PersistenceException;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static com.d4rksoul.base.ecommerce.spring.react.integration.TestData.order;
import static com.d4rksoul.base.ecommerce.spring.react.integration.TestData.orderItem;
import static com.d4rksoul.base.ecommerce.spring.react.integration.TestData.product;
import static com.d4rksoul.base.ecommerce.spring.react.integration.TestData.review;
import static com.d4rksoul.base.ecommerce.spring.react.integration.TestData.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductRepositoryIT extends AbstractIntegrationTest {

    private static final Pageable FIRST_PAGE = PageRequest.of(0, 10);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private void addSale(Product product, OrderStatus status, int quantity) {
        User buyer = persist(user());
        Order order = persist(order(buyer, status));

        persist(orderItem(order, product.getProductSkus().getFirst(), quantity));
    }

    private void addReview(Product product, int rating, boolean hidden) {
        User author = persist(user());

        persist(review(author, product, rating, hidden));
    }

    private void setCreatedAt(Product product, Instant createdAt) {
        jdbcTemplate.update(
            "update products set created_at = ? where id = ?", Timestamp.from(createdAt), product.getId()
        );
    }

    private static List<String> refsOf(Iterable<Product> products) {
        return java.util.stream.StreamSupport.stream(products.spliterator(), false)
            .map(Product::getRef)
            .toList();
    }

    @Test
    @DisplayName("visible details: returns an active product with an active sku and loads every sku")
    void visibleDetailsReturnsProductWithSkus() {
        Product product = addVisibleProduct("A");
        addSku(product, ProductSize.L, false, 5);
        flushAndClear();

        Optional<Product> found = productRepository.findVisibleDetailsById(product.getId());

        assertThat(found).isPresent();
        assertThat(Hibernate.isInitialized(found.orElseThrow().getProductSkus())).isTrue();
        assertThat(found.orElseThrow().getProductSkus()).hasSize(2);
    }

    @Test
    @DisplayName("visible details: an inactive product is not returned")
    void visibleDetailsIgnoresInactiveProduct() {
        Product product = addVisibleProduct("A");
        product.setActive(false);
        flushAndClear();

        assertThat(productRepository.findVisibleDetailsById(product.getId())).isEmpty();
    }

    @Test
    @DisplayName("visible details: a product whose skus are all inactive is not returned")
    void visibleDetailsIgnoresProductWithoutActiveSku() {
        Product product = addProduct("A");
        addSku(product, ProductSize.M, false, 10);
        flushAndClear();

        assertThat(productRepository.findVisibleDetailsById(product.getId())).isEmpty();
    }

    @Test
    @DisplayName("visible details: a product without skus is not returned")
    void visibleDetailsIgnoresProductWithoutSkus() {
        Product product = addProduct("A");
        flushAndClear();

        assertThat(productRepository.findVisibleDetailsById(product.getId())).isEmpty();
    }

    @Test
    @DisplayName("rating stats: averages and counts only the visible reviews")
    void ratingStatsIgnoreHiddenReviews() {
        Product product = addVisibleProduct("A");
        addReview(product, 4, false);
        addReview(product, 5, false);
        addReview(product, 1, true);
        flushAndClear();

        RatingStatsProjection stats = productRepository.findRatingStatsByProductId(product.getId());

        assertThat(stats.averageRating()).isEqualTo(4.5);
        assertThat(stats.totalReviews()).isEqualTo(2L);
    }

    @Test
    @DisplayName("rating stats: a product without reviews has average 0 and no reviews")
    void ratingStatsWithoutReviews() {
        Product product = addVisibleProduct("A");
        flushAndClear();

        RatingStatsProjection stats = productRepository.findRatingStatsByProductId(product.getId());

        assertThat(stats.averageRating()).isZero();
        assertThat(stats.totalReviews()).isZero();
    }

    @Test
    @DisplayName("best sellers: orders by quantity sold and ignores orders outside the given statuses")
    void bestSellersOrderByQuantitySold() {
        Product a = addVisibleProduct("A");
        Product b = addVisibleProduct("B");
        Product c = addVisibleProduct("C");
        addSale(a, OrderStatus.PAID, 5);
        addSale(b, OrderStatus.PAID, 2);
        addSale(b, OrderStatus.PENDING, 10);
        addSale(c, OrderStatus.DELIVERED, 3);
        flushAndClear();

        Page<Product> page = productRepository.findBestSellersPage(
            OrderStatus.HISTORY_STATUSES, null, FIRST_PAGE
        );

        assertThat(refsOf(page)).containsExactly("A", "C", "B");
    }

    @Test
    @DisplayName("best sellers: filters by category")
    void bestSellersFilterByCategory() {
        Product tops = addVisibleProduct("TOP");
        Product bottoms = addVisibleProduct("BOTTOM");
        bottoms.setCategory(ProductCategory.BOTTOMS);
        addSale(tops, OrderStatus.PAID, 1);
        addSale(bottoms, OrderStatus.PAID, 1);
        flushAndClear();

        Page<Product> page = productRepository.findBestSellersPage(
            OrderStatus.HISTORY_STATUSES, ProductCategory.BOTTOMS, FIRST_PAGE
        );

        assertThat(refsOf(page)).containsExactly("BOTTOM");
    }

    @Test
    @DisplayName("best sellers: leaves out inactive products and products without an active sku")
    void bestSellersExcludeUnavailableProducts() {
        Product available = addVisibleProduct("AVAILABLE");
        Product inactive = addVisibleProduct("INACTIVE");
        inactive.setActive(false);
        Product withoutActiveSku = addProduct("NO_SKU");
        addSku(withoutActiveSku, ProductSize.M, false, 10);
        addSale(available, OrderStatus.PAID, 1);
        addSale(inactive, OrderStatus.PAID, 9);
        addSale(withoutActiveSku, OrderStatus.PAID, 9);
        flushAndClear();

        Page<Product> page = productRepository.findBestSellersPage(
            OrderStatus.HISTORY_STATUSES, null, FIRST_PAGE
        );

        assertThat(refsOf(page)).containsExactly("AVAILABLE");
    }

    @Test
    @DisplayName("best sellers: the total counts every product, not only the current page")
    void bestSellersCountEveryProduct() {
        addSale(addVisibleProduct("A"), OrderStatus.PAID, 3);
        addSale(addVisibleProduct("B"), OrderStatus.PAID, 2);
        addSale(addVisibleProduct("C"), OrderStatus.PAID, 1);
        flushAndClear();

        Page<Product> page = productRepository.findBestSellersPage(
            OrderStatus.HISTORY_STATUSES, null, PageRequest.of(0, 2)
        );

        assertThat(refsOf(page)).containsExactly("A", "B");
        assertThat(page.getTotalElements()).isEqualTo(3);
    }

    @Test
    @DisplayName("top rated: orders by average, then by number of reviews, and requires the minimum reviews")
    void topRatedOrdersByAverageThenCount() {
        Product a = addVisibleProduct("A");
        Product b = addVisibleProduct("B");
        Product c = addVisibleProduct("C");
        Product d = addVisibleProduct("D");
        addReview(a, 5, false);
        addReview(a, 5, false);
        for (int i = 0; i < 3; i++) {
            addReview(b, 4, false);
        }
        addReview(c, 5, false);
        for (int i = 0; i < 4; i++) {
            addReview(d, 4, false);
        }
        flushAndClear();

        Page<Product> page = productRepository.findTopRatedPage(2, null, FIRST_PAGE);

        assertThat(refsOf(page)).containsExactly("A", "D", "B");
        assertThat(page.getTotalElements()).isEqualTo(3);
    }

    @Test
    @DisplayName("top rated: hidden reviews do not count towards the minimum")
    void topRatedIgnoresHiddenReviews() {
        Product product = addVisibleProduct("A");
        addReview(product, 5, false);
        addReview(product, 5, true);
        flushAndClear();

        Page<Product> page = productRepository.findTopRatedPage(2, null, FIRST_PAGE);

        assertThat(page.getContent()).isEmpty();
    }

    @Test
    @DisplayName("top rated: leaves out inactive products and products without an active sku")
    void topRatedExcludesUnavailableProducts() {
        Product inactive = addVisibleProduct("INACTIVE");
        inactive.setActive(false);
        Product withoutActiveSku = addProduct("NO_SKU");
        addSku(withoutActiveSku, ProductSize.M, false, 10);
        for (Product product : List.of(inactive, withoutActiveSku)) {
            addReview(product, 5, false);
            addReview(product, 5, false);
        }
        flushAndClear();

        Page<Product> page = productRepository.findTopRatedPage(2, null, FIRST_PAGE);

        assertThat(page.getContent()).isEmpty();
    }

    @Test
    @DisplayName("top rated: filters by category")
    void topRatedFilterByCategory() {
        Product tops = addVisibleProduct("TOP");
        Product bottoms = addVisibleProduct("BOTTOM");
        bottoms.setCategory(ProductCategory.BOTTOMS);
        for (Product product : List.of(tops, bottoms)) {
            addReview(product, 5, false);
            addReview(product, 5, false);
        }
        flushAndClear();

        Page<Product> page = productRepository.findTopRatedPage(
            2, ProductCategory.TOPS, FIRST_PAGE
        );

        assertThat(refsOf(page)).containsExactly("TOP");
    }

    @Test
    @DisplayName("featured slot: finds the product that holds the slot")
    void findsProductByFeaturedSlot() {
        Product product = addProduct("A");
        product.setFeaturedSlot(FeaturedSlot.LEFT);
        flushAndClear();

        assertThat(productRepository.findByFeaturedSlot(FeaturedSlot.LEFT)).isPresent();
        assertThat(productRepository.findByFeaturedSlot(FeaturedSlot.RIGHT)).isEmpty();
    }

    @Test
    @DisplayName("featured slot: the database does not allow two products in the same slot")
    void featuredSlotIsUnique() {
        Product first = addProduct("A");
        first.setFeaturedSlot(FeaturedSlot.LEFT);
        flushAndClear();

        Product second = product("B");
        second.setFeaturedSlot(FeaturedSlot.LEFT);

        assertThatThrownBy(() -> persist(second))
            .isInstanceOf(PersistenceException.class)
            .hasMessageContaining("uk_product_featured_slot");
    }

    @Test
    @DisplayName("ref: existsByRef tells whether a ref is already taken")
    void existsByRef() {
        addProduct("A");
        flushAndClear();

        assertThat(productRepository.existsByRef("A")).isTrue();
        assertThat(productRepository.existsByRef("OTHER")).isFalse();
    }

    @Test
    @DisplayName("ref: existsByRefAndIdNot ignores the product itself")
    void existsByRefAndIdNotIgnoresItself() {
        Product first = addProduct("A");
        Product second = addProduct("B");
        flushAndClear();

        assertThat(productRepository.existsByRefAndIdNot("A", first.getId())).isFalse();
        assertThat(productRepository.existsByRefAndIdNot("A", second.getId())).isTrue();
    }

    @Test
    @DisplayName("ref: the database does not allow two products with the same ref")
    void refIsUnique() {
        addProduct("A");
        flushAndClear();

        Product duplicate = product("A");

        assertThatThrownBy(() -> persist(duplicate)).isInstanceOf(PersistenceException.class);
    }

    @Test
    @DisplayName("spec isVisibleInShop: only active products with at least one active sku")
    void isVisibleInShop() {
        addVisibleProduct("VISIBLE");
        Product inactive = addVisibleProduct("INACTIVE");
        inactive.setActive(false);
        Product withoutActiveSku = addProduct("NO_ACTIVE_SKU");
        addSku(withoutActiveSku, ProductSize.M, false, 10);
        addProduct("NO_SKU");
        flushAndClear();

        List<Product> found = productRepository.findAll(ProductSpecification.isVisibleInShop());

        assertThat(refsOf(found)).containsExactly("VISIBLE");
    }

    @Test
    @DisplayName("spec containsNameOrRef: matches the name ignoring case")
    void containsNameOrRefMatchesName() {
        Product jacket = addProduct("A");
        jacket.setName("Rain Jacket");
        addProduct("B");
        flushAndClear();

        List<Product> found = productRepository.findAll(
            ProductSpecification.containsNameOrRef("  JACKET ")
        );

        assertThat(refsOf(found)).containsExactly("A");
    }

    @Test
    @DisplayName("spec containsNameOrRef: matches the ref ignoring case")
    void containsNameOrRefMatchesRef() {
        addProduct("JKT-001");
        addProduct("TEE-002");
        flushAndClear();

        List<Product> found = productRepository.findAll(ProductSpecification.containsNameOrRef("jkt"));

        assertThat(refsOf(found)).containsExactly("JKT-001");
    }

    @Test
    @DisplayName("spec containsNameOrRef: a blank text does not filter")
    void containsNameOrRefBlankDoesNotFilter() {
        addProduct("A");
        addProduct("B");
        flushAndClear();

        assertThat(productRepository.findAll(ProductSpecification.containsNameOrRef("  ")))
            .hasSize(2);
        assertThat(productRepository.findAll(ProductSpecification.containsNameOrRef(null)))
            .hasSize(2);
    }

    @Test
    @DisplayName("spec hasCategory: filters by category and ignores a null category")
    void hasCategory() {
        addProduct("TOP");
        Product bottoms = addProduct("BOTTOM");
        bottoms.setCategory(ProductCategory.BOTTOMS);
        flushAndClear();

        List<Product> filtered = productRepository.findAll(
            ProductSpecification.hasCategory(ProductCategory.BOTTOMS)
        );
        List<Product> everything = productRepository.findAll(ProductSpecification.hasCategory(null));

        assertThat(refsOf(filtered)).containsExactly("BOTTOM");
        assertThat(everything).hasSize(2);
    }

    @Test
    @DisplayName("spec hasFeaturedSlot: only products that hold a slot")
    void hasFeaturedSlot() {
        Product left = addProduct("LEFT");
        left.setFeaturedSlot(FeaturedSlot.LEFT);
        Product right = addProduct("RIGHT");
        right.setFeaturedSlot(FeaturedSlot.RIGHT);
        addProduct("NONE");
        flushAndClear();

        List<Product> found = productRepository.findAll(ProductSpecification.hasFeaturedSlot());

        assertThat(refsOf(found)).containsExactlyInAnyOrder("LEFT", "RIGHT");
    }

    @Test
    @DisplayName("spec hasIdNot: leaves out the given product")
    void hasIdNot() {
        Product first = addProduct("A");
        addProduct("B");
        flushAndClear();

        List<Product> found = productRepository.findAll(ProductSpecification.hasIdNot(first.getId()));

        assertThat(refsOf(found)).containsExactly("B");
    }

    @Test
    @DisplayName("spec hasDiscount: only products with a discount above zero")
    void hasDiscount() {
        Product discounted = addProduct("DISCOUNTED");
        discounted.setDiscountPercentage(20);
        addProduct("FULL_PRICE");
        flushAndClear();

        List<Product> found = productRepository.findAll(ProductSpecification.hasDiscount());

        assertThat(refsOf(found)).containsExactly("DISCOUNTED");
    }

    @Test
    @DisplayName("spec hasLowStock: counts only active skus at or below the threshold")
    void hasLowStockOnlyCountsActiveSkus() {
        Product low = addProduct("LOW");
        addSku(low, ProductSize.M, true, 3);
        Product plenty = addProduct("PLENTY");
        addSku(plenty, ProductSize.M, true, 20);
        Product inactiveLow = addProduct("INACTIVE_LOW");
        addSku(inactiveLow, ProductSize.M, false, 1);
        Product mixed = addProduct("MIXED");
        addSku(mixed, ProductSize.M, true, 50);
        addSku(mixed, ProductSize.L, true, 5);
        flushAndClear();

        List<Product> found = productRepository.findAll(
            ProductSpecification.hasLowStock(true, 5)
        );

        assertThat(refsOf(found)).containsExactlyInAnyOrder("LOW", "MIXED");
    }

    @Test
    @DisplayName("spec hasLowStock: does not filter when low stock is not requested")
    void hasLowStockDisabledDoesNotFilter() {
        Product plenty = addProduct("PLENTY");
        addSku(plenty, ProductSize.M, true, 20);
        flushAndClear();

        assertThat(productRepository.findAll(
            ProductSpecification.hasLowStock(false, 5))
        ).hasSize(1);
    }

    private Specification<Product> visibleWithPriceOrder(String sortBy) {
        return Specification
            .where(ProductSpecification.isVisibleInShop())
            .and(ProductSpecification.orderByEffectivePrice(sortBy));
    }

    private void addProductsForPriceOrder() {
        Product expensive = addVisibleProduct("A");
        expensive.setPrice(new BigDecimal("100.00"));
        Product halfPrice = addVisibleProduct("B");
        halfPrice.setPrice(new BigDecimal("120.00"));
        halfPrice.setDiscountPercentage(50);
        Product cheap = addVisibleProduct("C");
        cheap.setPrice(new BigDecimal("80.00"));
        flushAndClear();
    }

    @Test
    @DisplayName("spec orderByEffectivePrice: ascending sorts by the price after the discount")
    void orderByEffectivePriceAscending() {
        addProductsForPriceOrder();

        List<Product> found = productRepository.findAll(visibleWithPriceOrder("price_asc"));

        assertThat(refsOf(found)).containsExactly("B", "C", "A");
    }

    @Test
    @DisplayName("spec orderByEffectivePrice: descending sorts by the price after the discount")
    void orderByEffectivePriceDescending() {
        addProductsForPriceOrder();

        List<Product> found = productRepository.findAll(visibleWithPriceOrder("price_desc"));

        assertThat(refsOf(found)).containsExactly("A", "C", "B");
    }

    @Test
    @DisplayName("spec orderByEffectivePrice: works with pagination and counts every product")
    void orderByEffectivePriceWithPagination() {
        addProductsForPriceOrder();

        Page<Product> page = productRepository.findAll(
            visibleWithPriceOrder("price_asc"), PageRequest.of(0, 2)
        );

        assertThat(refsOf(page)).containsExactly("B", "C");
        assertThat(page.getTotalElements()).isEqualTo(3);
    }

    @Test
    @DisplayName("spec orderByDiscountDesc: highest discount first")
    void orderByDiscountDesc() {
        Product small = addProduct("SMALL");
        small.setDiscountPercentage(10);
        Product big = addProduct("BIG");
        big.setDiscountPercentage(40);
        flushAndClear();

        List<Product> found = productRepository.findAll(ProductSpecification.orderByDiscountDesc());

        assertThat(refsOf(found)).containsExactly("BIG", "SMALL");
    }

    @Test
    @DisplayName("spec orderByRecentDesc: newest product first")
    void orderByRecentDesc() {
        Product oldest = addProduct("OLDEST");
        Product newest = addProduct("NEWEST");
        Product middle = addProduct("MIDDLE");
        Instant now = Instant.now();
        em.flush();

        setCreatedAt(oldest, now.minus(3, ChronoUnit.DAYS));
        setCreatedAt(middle, now.minus(2, ChronoUnit.DAYS));
        setCreatedAt(newest, now.minus(1, ChronoUnit.DAYS));
        em.clear();

        List<Product> found = productRepository.findAll(ProductSpecification.orderByRecentDesc());

        assertThat(refsOf(found)).containsExactly("NEWEST", "MIDDLE", "OLDEST");
    }
}
