package com.d4rksoul.base.ecommerce.spring.react.persistence;

import com.d4rksoul.base.ecommerce.spring.react.dto.projection.CategoryUnitsProjection;
import com.d4rksoul.base.ecommerce.spring.react.dto.projection.CustomerOrderStatsProjection;
import com.d4rksoul.base.ecommerce.spring.react.dto.projection.DailyTotalProjection;
import com.d4rksoul.base.ecommerce.spring.react.dto.projection.DiscountCodeUsageProjection;
import com.d4rksoul.base.ecommerce.spring.react.dto.projection.StatusCountProjection;
import com.d4rksoul.base.ecommerce.spring.react.dto.projection.TopDiscountCodeProjection;
import com.d4rksoul.base.ecommerce.spring.react.dto.projection.TopProductProjection;
import com.d4rksoul.base.ecommerce.spring.react.entity.DiscountCode;
import com.d4rksoul.base.ecommerce.spring.react.entity.Order;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderItem;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderReturn;
import com.d4rksoul.base.ecommerce.spring.react.entity.Product;
import com.d4rksoul.base.ecommerce.spring.react.entity.Review;
import com.d4rksoul.base.ecommerce.spring.react.entity.User;
import com.d4rksoul.base.ecommerce.spring.react.enums.OrderStatus;
import com.d4rksoul.base.ecommerce.spring.react.enums.ProductCategory;
import com.d4rksoul.base.ecommerce.spring.react.enums.ReturnStatus;
import com.d4rksoul.base.ecommerce.spring.react.enums.UserRole;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderItemRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderReturnRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.ReviewRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.d4rksoul.base.ecommerce.spring.react.persistence.TestData.discountCode;
import static com.d4rksoul.base.ecommerce.spring.react.persistence.TestData.order;
import static com.d4rksoul.base.ecommerce.spring.react.persistence.TestData.orderItem;
import static com.d4rksoul.base.ecommerce.spring.react.persistence.TestData.orderReturn;
import static com.d4rksoul.base.ecommerce.spring.react.persistence.TestData.review;
import static com.d4rksoul.base.ecommerce.spring.react.persistence.TestData.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class AdminDashboardQueriesIT extends AbstractIntegrationTest {

    private static final Instant START = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant END = Instant.parse("2026-02-01T00:00:00Z");
    private static final Instant EMPTY_START = Instant.parse("2025-01-01T00:00:00Z");
    private static final Instant EMPTY_END = Instant.parse("2025-02-01T00:00:00Z");

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderReturnRepository orderReturnRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static Instant at(String instant) {
        return Instant.parse(instant);
    }

    private Order addPaidOrder(OrderStatus status, Instant paidAt, String total) {
        Order order = order(addUser(), status);
        order.setPaidAt(paidAt);
        order.setTotalAmount(new BigDecimal(total));

        return persist(order);
    }

    private void addPaidOrderWithCode(DiscountCode code, OrderStatus status, Instant paidAt) {
        Order order = order(addUser(), status);
        order.setPaidAt(paidAt);
        order.setDiscountCode(code);

        persist(order);
    }

    private void addSale(Product product, int quantity, OrderStatus status, Instant paidAt) {
        Order order = addPaidOrder(status, paidAt, "10.00");
        addOrderItem(order, product.getProductSkus().getFirst(), quantity);
    }

    private void addRefund(ReturnStatus status, String amount, Instant refundedAt) {
        OrderReturn orderReturn = orderReturn(addOrder(addUser(), OrderStatus.DELIVERED), status);
        orderReturn.setRefundAmount(new BigDecimal(amount));
        orderReturn.setRefundedAt(refundedAt);

        persist(orderReturn);
    }

    private void setOrderCreatedAt(Order order, Instant createdAt) {
        jdbcTemplate.update(
            "update orders set created_at = ? where id = ?",
            Timestamp.from(createdAt),
            order.getId()
        );
    }

    private void setReviewCreatedAt(Review review, Instant createdAt) {
        jdbcTemplate.update(
            "update reviews set created_at = ? where id = ?",
            Timestamp.from(createdAt),
            review.getId()
        );
    }

    private void setUserCreatedAt(User user, Instant createdAt) {
        jdbcTemplate.update(
            "update users set created_at = ? where id = ?",
            Timestamp.from(createdAt),
            user.getId()
        );
    }

    private static Map<LocalDate, DailyTotalProjection> byDay(List<DailyTotalProjection> days) {
        return days.stream().collect(Collectors.toMap(
            day ->
                LocalDate.of(day.year(), day.month(), day.day()), Function.identity()
        ));
    }

    @Test
    @DisplayName("orders in period: counts the given statuses paid from the start, up to but excluding the end")
    void countInPeriod() {
        addPaidOrder(OrderStatus.PAID, START, "1.00");
        addPaidOrder(OrderStatus.DELIVERED, END.minusSeconds(1), "1.00");
        addPaidOrder(OrderStatus.PAID, END, "1.00");
        addPaidOrder(OrderStatus.PAID, START.minusSeconds(1), "1.00");
        addPaidOrder(OrderStatus.PENDING, START.plusSeconds(60), "1.00");
        flushAndClear();

        long count = orderRepository.countInPeriod(OrderStatus.HISTORY_STATUSES, START, END);

        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("orders by status in period: one count per status")
    void countByStatusInPeriod() {
        addPaidOrder(OrderStatus.PAID, START.plusSeconds(60), "1.00");
        addPaidOrder(OrderStatus.PAID, START.plusSeconds(120), "1.00");
        addPaidOrder(OrderStatus.SHIPPED, START.plusSeconds(180), "1.00");
        addPaidOrder(OrderStatus.PAID, END.plusSeconds(60), "1.00");
        flushAndClear();

        List<StatusCountProjection> counts = orderRepository.countByStatusInPeriod(
            OrderStatus.HISTORY_STATUSES, START, END
        );

        assertThat(counts)
            .extracting(StatusCountProjection::status, StatusCountProjection::count)
            .containsExactlyInAnyOrder(
                tuple(OrderStatus.PAID, 2L),
                tuple(OrderStatus.SHIPPED, 1L)
            );
    }

    @Test
    @DisplayName("sales in period: sums the totals of the given statuses and gives 0 when there are none")
    void sumTotalAmountInPeriod() {
        addPaidOrder(OrderStatus.PAID, START.plusSeconds(60), "10.50");
        addPaidOrder(OrderStatus.DELIVERED, START.plusSeconds(120), "4.50");
        addPaidOrder(OrderStatus.PAID, END.plusSeconds(60), "99.00");
        addPaidOrder(OrderStatus.PENDING, START.plusSeconds(180), "50.00");
        flushAndClear();

        BigDecimal total = orderRepository.sumTotalAmountInPeriod(OrderStatus.HISTORY_STATUSES, START, END);
        BigDecimal none = orderRepository.sumTotalAmountInPeriod(
            OrderStatus.HISTORY_STATUSES, EMPTY_START, EMPTY_END
        );

        assertThat(total).isEqualByComparingTo("15.00");
        assertThat(none).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("sales by day: groups by UTC day with the total and the number of orders")
    void sumTotalAmountByDay() {
        addPaidOrder(OrderStatus.PAID, at("2026-01-10T23:30:00Z"), "10.00");
        addPaidOrder(OrderStatus.DELIVERED, at("2026-01-10T08:00:00Z"), "5.00");
        addPaidOrder(OrderStatus.PAID, at("2026-01-11T00:30:00Z"), "7.00");
        addPaidOrder(OrderStatus.PENDING, at("2026-01-11T09:00:00Z"), "50.00");
        addPaidOrder(OrderStatus.PAID, at("2026-03-01T09:00:00Z"), "99.00");
        flushAndClear();

        Map<LocalDate, DailyTotalProjection> days = byDay(
            orderRepository.sumTotalAmountByDay(OrderStatus.HISTORY_STATUSES, START, END)
        );

        assertThat(days).containsOnlyKeys(
            LocalDate.of(2026, 1, 10),
            LocalDate.of(2026, 1, 11)
        );
        assertThat(days.get(LocalDate.of(2026, 1, 10)).total())
            .isEqualByComparingTo("15.00");
        assertThat(days.get(LocalDate.of(2026, 1, 10)).count())
            .isEqualTo(2L);
        assertThat(days.get(LocalDate.of(2026, 1, 11)).total())
            .isEqualByComparingTo("7.00");
        assertThat(days.get(LocalDate.of(2026, 1, 11)).count())
            .isEqualTo(1L);
    }

    @Test
    @DisplayName("top discount codes: most used first, ties by code, limited by the page and the period")
    void findTopDiscountCodes() {
        DiscountCode codeA = persist(discountCode("A"));
        DiscountCode codeB = persist(discountCode("B"));
        DiscountCode codeC = persist(discountCode("C"));
        DiscountCode codeD = persist(discountCode("D"));
        for (int i = 0; i < 2; i++) {
            addPaidOrderWithCode(codeA, OrderStatus.PAID, START.plusSeconds(60));
            addPaidOrderWithCode(codeC, OrderStatus.PAID, START.plusSeconds(60));
        }
        addPaidOrderWithCode(codeB, OrderStatus.PAID, START.plusSeconds(60));
        for (int i = 0; i < 5; i++) {
            addPaidOrderWithCode(codeD, OrderStatus.PAID, END.plusSeconds(60));
        }
        flushAndClear();

        List<TopDiscountCodeProjection> top = orderRepository.findTopDiscountCodes(
            OrderStatus.HISTORY_STATUSES, START, END, PageRequest.of(0, 2)
        );

        assertThat(top)
            .extracting(TopDiscountCodeProjection::code, TopDiscountCodeProjection::orders)
            .containsExactly(tuple("A", 2L), tuple("C", 2L));
    }

    @Test
    @DisplayName("customer stats: number of orders and date of the last one, only for the given statuses and users")
    void findCustomerStatsByUserIds() {
        User first = addUser();
        User second = addUser();
        User notRequested = addUser();
        Order firstOld = addOrder(first, OrderStatus.PAID);
        Order firstLast = addOrder(first, OrderStatus.DELIVERED);
        Order firstPending = addOrder(first, OrderStatus.PENDING);
        Order secondOnly = addOrder(second, OrderStatus.PAID);
        Order ignored = addOrder(notRequested, OrderStatus.PAID);
        setOrderCreatedAt(firstOld, at("2026-01-05T10:00:00Z"));
        setOrderCreatedAt(firstLast, at("2026-01-07T10:00:00Z"));
        setOrderCreatedAt(firstPending, at("2026-01-09T10:00:00Z"));
        setOrderCreatedAt(secondOnly, at("2026-01-06T10:00:00Z"));
        setOrderCreatedAt(ignored, at("2026-01-08T10:00:00Z"));
        flushAndClear();

        List<CustomerOrderStatsProjection> stats = orderRepository.findCustomerStatsByUserIds(
            List.of(first.getId(), second.getId()), OrderStatus.HISTORY_STATUSES
        );

        assertThat(stats)
            .extracting(
                CustomerOrderStatsProjection::userId,
                CustomerOrderStatsProjection::orders,
                CustomerOrderStatsProjection::lastOrderAt
            )
            .containsExactlyInAnyOrder(
                tuple(first.getId(), 2L, at("2026-01-07T10:00:00Z")),
                tuple(second.getId(), 1L, at("2026-01-06T10:00:00Z"))
            );
    }

    @Test
    @DisplayName("discount code usage: counts the orders of each requested code")
    void countOrdersByDiscountCodeIds() {
        DiscountCode codeA = persist(discountCode("A"));
        DiscountCode codeB = persist(discountCode("B"));
        DiscountCode notRequested = persist(discountCode("C"));
        addPaidOrderWithCode(codeA, OrderStatus.PAID, START);
        addPaidOrderWithCode(codeA, OrderStatus.CANCELED, START);
        addPaidOrderWithCode(codeB, OrderStatus.PAID, START);
        addPaidOrderWithCode(notRequested, OrderStatus.PAID, START);
        addPaidOrder(OrderStatus.PAID, START, "1.00");
        flushAndClear();

        List<DiscountCodeUsageProjection> usage = orderRepository.countOrdersByDiscountCodeIds(
            List.of(codeA.getId(), codeB.getId())
        );

        assertThat(usage)
            .extracting(DiscountCodeUsageProjection::discountCodeId, DiscountCodeUsageProjection::orders)
            .containsExactlyInAnyOrder(tuple(codeA.getId(), 2L), tuple(codeB.getId(), 1L));
    }

    @Test
    @DisplayName("top products: most units first, ties by product id, only the given statuses and period")
    void findTopProducts() {
        Product first = addVisibleProduct("P1");
        Product second = addVisibleProduct("P2");
        Product third = addVisibleProduct("P3");
        Product outsidePeriod = addVisibleProduct("P4");
        Product pending = addVisibleProduct("P5");
        addSale(first, 3, OrderStatus.PAID, START.plusSeconds(60));
        addSale(first, 2, OrderStatus.DELIVERED, START.plusSeconds(120));
        addSale(second, 6, OrderStatus.PAID, START.plusSeconds(60));
        addSale(third, 5, OrderStatus.PAID, START.plusSeconds(60));
        addSale(outsidePeriod, 100, OrderStatus.PAID, END.plusSeconds(60));
        addSale(pending, 100, OrderStatus.PENDING, START.plusSeconds(60));
        flushAndClear();

        List<TopProductProjection> top = orderItemRepository.findTopProducts(
            OrderStatus.HISTORY_STATUSES, START, END, PageRequest.of(0, 3)
        );

        assertThat(top)
            .extracting(TopProductProjection::productId, TopProductProjection::unitsSold)
            .containsExactly(
                tuple(second.getId(), 6L),
                tuple(first.getId(), 5L),
                tuple(third.getId(), 5L)
            );
    }

    @Test
    @DisplayName("units by category: most units first")
    void sumUnitsByCategory() {
        Product tops = addVisibleProduct("TOP");
        Product moreTops = addVisibleProduct("TOP2");
        Product bottoms = addVisibleProduct("BOTTOM");
        bottoms.setCategory(ProductCategory.BOTTOMS);
        addSale(tops, 3, OrderStatus.PAID, START.plusSeconds(60));
        addSale(moreTops, 2, OrderStatus.PAID, START.plusSeconds(60));
        addSale(bottoms, 7, OrderStatus.PAID, START.plusSeconds(60));
        flushAndClear();

        List<CategoryUnitsProjection> units = orderItemRepository.sumUnitsByCategory(
            OrderStatus.HISTORY_STATUSES, START, END
        );

        assertThat(units)
            .extracting(CategoryUnitsProjection::category, CategoryUnitsProjection::unitsSold)
            .containsExactly(
                tuple(ProductCategory.BOTTOMS, 7L),
                tuple(ProductCategory.TOPS, 5L)
            );
    }

    @Test
    @DisplayName("image used by orders: true only for a path that an order item kept")
    void existsByImagePath() {
        Product product = addVisibleProduct("A");
        Order order = addOrder(addUser(), OrderStatus.PAID);
        OrderItem item = orderItem(order, product.getProductSkus().getFirst(), 1);
        item.setImagePath("used.webp");
        persist(item);
        flushAndClear();

        assertThat(orderItemRepository.existsByImagePath("used.webp")).isTrue();
        assertThat(orderItemRepository.existsByImagePath("other.webp")).isFalse();
    }

    @Test
    @DisplayName("refunds in period: sums the refunded returns of the period and gives 0 when there are none")
    void sumRefundAmountInPeriod() {
        addRefund(ReturnStatus.REFUNDED, "10.00", START.plusSeconds(60));
        addRefund(ReturnStatus.REFUNDED, "5.50", START.plusSeconds(120));
        addRefund(ReturnStatus.REFUNDED, "99.00", END.plusSeconds(60));
        addRefund(ReturnStatus.REQUESTED, "50.00", START.plusSeconds(180));
        flushAndClear();

        BigDecimal total = orderReturnRepository.sumRefundAmountInPeriod(ReturnStatus.REFUNDED, START, END);
        BigDecimal none = orderReturnRepository.sumRefundAmountInPeriod(
            ReturnStatus.REFUNDED, EMPTY_START, EMPTY_END
        );

        assertThat(total).isEqualByComparingTo("15.50");
        assertThat(none).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("refunds by day: groups by UTC day with the total and the number of refunds")
    void sumRefundAmountByDay() {
        addRefund(ReturnStatus.REFUNDED, "10.00", at("2026-01-10T23:30:00Z"));
        addRefund(ReturnStatus.REFUNDED, "2.00", at("2026-01-10T08:00:00Z"));
        addRefund(ReturnStatus.REFUNDED, "5.50", at("2026-01-11T00:30:00Z"));
        flushAndClear();

        Map<LocalDate, DailyTotalProjection> days = byDay(
            orderReturnRepository.sumRefundAmountByDay(ReturnStatus.REFUNDED, START, END)
        );

        assertThat(days).containsOnlyKeys(
            LocalDate.of(2026, 1, 10),
            LocalDate.of(2026, 1, 11)
        );
        assertThat(days.get(LocalDate.of(2026, 1, 10)).total())
            .isEqualByComparingTo("12.00");
        assertThat(days.get(LocalDate.of(2026, 1, 10)).count())
            .isEqualTo(2L);
        assertThat(days.get(LocalDate.of(2026, 1, 11)).total())
            .isEqualByComparingTo("5.50");
    }

    @Test
    @DisplayName("reviews in period: counts and averages only the visible reviews created in the period")
    void reviewsInPeriod() {
        Product product = addVisibleProduct("A");
        Review four = persist(review(addUser(), product, 4, false));
        Review five = persist(review(addUser(), product, 5, false));
        Review hidden = persist(review(addUser(), product, 1, true));
        Review outside = persist(review(addUser(), product, 1, false));
        setReviewCreatedAt(four, START.plusSeconds(60));
        setReviewCreatedAt(five, START.plusSeconds(120));
        setReviewCreatedAt(hidden, START.plusSeconds(180));
        setReviewCreatedAt(outside, END.plusSeconds(60));
        flushAndClear();

        assertThat(reviewRepository.countInPeriod(START, END)).isEqualTo(2);
        assertThat(reviewRepository.averageRatingInPeriod(START, END)).isEqualTo(4.5);
    }

    @Test
    @DisplayName("reviews in period: with no reviews the count is 0 and there is no average")
    void reviewsInPeriodWithoutReviews() {
        assertThat(reviewRepository.countInPeriod(EMPTY_START, EMPTY_END)).isZero();
        assertThat(reviewRepository.averageRatingInPeriod(EMPTY_START, EMPTY_END)).isNull();
    }

    @Test
    @DisplayName("new customers in period: counts only users of that role created in the period")
    void countByRoleInPeriod() {
        User inside = addUser();
        User alsoInside = addUser();
        User outside = addUser();
        User manager = user();
        manager.setRole(UserRole.MANAGER);
        persist(manager);
        setUserCreatedAt(inside, START.plusSeconds(60));
        setUserCreatedAt(alsoInside, END.minusSeconds(1));
        setUserCreatedAt(outside, END);
        setUserCreatedAt(manager, START.plusSeconds(60));
        flushAndClear();

        long customers = userRepository.countByRoleInPeriod(UserRole.CUSTOMER, START, END);

        assertThat(customers).isEqualTo(2);
    }
}
