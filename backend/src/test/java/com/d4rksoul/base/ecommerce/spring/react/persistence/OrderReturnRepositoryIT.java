package com.d4rksoul.base.ecommerce.spring.react.persistence;

import com.d4rksoul.base.ecommerce.spring.react.dto.projection.ReturnedQuantityProjection;
import com.d4rksoul.base.ecommerce.spring.react.entity.Order;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderItem;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderReturn;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderReturnItem;
import com.d4rksoul.base.ecommerce.spring.react.entity.Product;
import com.d4rksoul.base.ecommerce.spring.react.entity.ProductSku;
import com.d4rksoul.base.ecommerce.spring.react.enums.OrderStatus;
import com.d4rksoul.base.ecommerce.spring.react.enums.ProductSize;
import com.d4rksoul.base.ecommerce.spring.react.enums.ReturnStatus;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderReturnItemRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderReturnRepository;
import com.d4rksoul.base.ecommerce.spring.react.specification.ReturnSpecification;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.d4rksoul.base.ecommerce.spring.react.persistence.TestData.orderReturn;
import static com.d4rksoul.base.ecommerce.spring.react.persistence.TestData.orderReturnItem;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class OrderReturnRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private OrderReturnRepository orderReturnRepository;

    @Autowired
    private OrderReturnItemRepository orderReturnItemRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;


    private Order addDeliveredOrder() {
        return addOrder(addUser(), OrderStatus.DELIVERED);
    }

    private OrderReturn addReturn(Order order, ReturnStatus status) {
        return persist(orderReturn(order, status));
    }

    private void addReturnItem(OrderReturn orderReturn, OrderItem orderItem, int quantity) {
        persist(orderReturnItem(orderReturn, orderItem, quantity));
    }

    private OrderReturn addReturnCreatedAt(Order order, ReturnStatus status, Instant createdAt) {
        OrderReturn orderReturn = addReturn(order, status);
        jdbcTemplate.update(
            "update order_returns set created_at = ? where id = ?",
            Timestamp.from(createdAt),
            orderReturn.getId()
        );

        return orderReturn;
    }

    private static List<Long> idsOf(List<OrderReturn> returns) {
        return returns.stream().map(OrderReturn::getId).toList();
    }

    @Test
    @DisplayName("returned quantities: sums per order item and leaves out the excluded statuses")
    void sumReturnedQuantitiesPerOrderItem() {
        Product product = addProduct("A");
        ProductSku skuM = addSku(product, ProductSize.M, true, 10);
        ProductSku skuL = addSku(product, ProductSize.L, true, 10);
        Order order = addDeliveredOrder();
        OrderItem first = addOrderItem(order, skuM, 5);
        OrderItem second = addOrderItem(order, skuL, 3);
        OrderReturn refunded = addReturn(order, ReturnStatus.REFUNDED);
        addReturnItem(refunded, first, 2);
        OrderReturn requested = addReturn(order, ReturnStatus.REQUESTED);
        addReturnItem(requested, first, 1);
        addReturnItem(requested, second, 1);
        OrderReturn rejected = addReturn(order, ReturnStatus.REJECTED);
        addReturnItem(rejected, first, 2);
        flushAndClear();

        List<ReturnedQuantityProjection> returned = orderReturnItemRepository.sumReturnedQuantities(
            order.getId(), ReturnStatus.INACTIVE_STATUSES
        );

        assertThat(returned)
            .extracting(ReturnedQuantityProjection::orderItemId, ReturnedQuantityProjection::quantity)
            .containsExactlyInAnyOrder(tuple(first.getId(), 3L), tuple(second.getId(), 1L));
    }

    @Test
    @DisplayName("returned quantities: the returns of other orders are not counted")
    void sumReturnedQuantitiesIgnoresOtherOrders() {
        ProductSku sku = addSku(addProduct("A"), ProductSize.M, true, 10);
        Order order = addDeliveredOrder();
        OrderItem item = addOrderItem(order, sku, 5);
        Order other = addDeliveredOrder();
        OrderItem otherItem = addOrderItem(other, sku, 5);
        addReturnItem(addReturn(other, ReturnStatus.REQUESTED), otherItem, 4);
        flushAndClear();

        List<ReturnedQuantityProjection> returned = orderReturnItemRepository.sumReturnedQuantities(
            order.getId(), ReturnStatus.INACTIVE_STATUSES
        );

        assertThat(item.getId()).isNotNull();
        assertThat(returned).isEmpty();
    }

    @Test
    @DisplayName("returned quantities: an order without returns has nothing returned")
    void sumReturnedQuantitiesWithoutReturns() {
        Order order = addDeliveredOrder();
        flushAndClear();

        assertThat(orderReturnItemRepository.sumReturnedQuantities(
            order.getId(), ReturnStatus.INACTIVE_STATUSES)
        ).isEmpty();
    }

    @Test
    @DisplayName("return items by return: loads the order item of each line")
    void findItemsByReturnLoadsTheOrderItem() {
        ProductSku sku = addSku(addProduct("A"), ProductSize.M, true, 10);
        Order order = addDeliveredOrder();
        OrderItem item = addOrderItem(order, sku, 2);
        OrderReturn orderReturn = addReturn(order, ReturnStatus.REQUESTED);
        addReturnItem(orderReturn, item, 1);
        flushAndClear();

        List<OrderReturnItem> items = orderReturnItemRepository.findByOrderReturnId(orderReturn.getId());

        assertThat(items).hasSize(1);
        assertThat(Hibernate.isInitialized(items.getFirst().getOrderItem())).isTrue();
    }

    @Test
    @DisplayName("return items of several returns: ordered by id with the order item loaded")
    void findItemsOfSeveralReturns() {
        Product product = addProduct("A");
        ProductSku skuM = addSku(product, ProductSize.M, true, 10);
        ProductSku skuL = addSku(product, ProductSize.L, true, 10);
        Order order = addDeliveredOrder();
        OrderItem first = addOrderItem(order, skuM, 2);
        OrderItem second = addOrderItem(order, skuL, 2);
        OrderReturn firstReturn = addReturn(order, ReturnStatus.REQUESTED);
        addReturnItem(firstReturn, first, 1);
        OrderReturn secondReturn = addReturn(order, ReturnStatus.REQUESTED);
        addReturnItem(secondReturn, second, 1);
        flushAndClear();

        List<OrderReturnItem> items = orderReturnItemRepository.findByOrderReturnInOrderByIdAsc(
            List.of(secondReturn, firstReturn)
        );

        assertThat(items).extracting(item -> item.getOrderItem().getId())
            .containsExactly(first.getId(), second.getId());
    }

    @Test
    @DisplayName("find by id and order: the return must belong to that order")
    void findByIdAndOrderId() {
        Order order = addDeliveredOrder();
        Order other = addDeliveredOrder();
        OrderReturn orderReturn = addReturn(order, ReturnStatus.REQUESTED);
        flushAndClear();

        assertThat(orderReturnRepository.findByIdAndOrderId(orderReturn.getId(), order.getId()))
            .isPresent();
        assertThat(orderReturnRepository.findByIdAndOrderId(orderReturn.getId(), other.getId()))
            .isEmpty();
    }

    @Test
    @DisplayName("find by id and status: locks and returns the return only in that status")
    void findByIdAndStatusWithLock() {
        OrderReturn orderReturn = addReturn(addDeliveredOrder(), ReturnStatus.REQUESTED);
        flushAndClear();

        assertThat(orderReturnRepository.findByIdAndStatus(orderReturn.getId(), ReturnStatus.REQUESTED))
            .isPresent();
        assertThat(orderReturnRepository.findByIdAndStatus(orderReturn.getId(), ReturnStatus.APPROVED))
            .isEmpty();
    }

    @Test
    @DisplayName("requested before limit: same status, created before the limit and not manually managed")
    void findRequestedBeforeLimit() {
        Instant now = Instant.now();
        OrderReturn match = addReturnCreatedAt(
            addDeliveredOrder(), ReturnStatus.REQUESTED, now.minus(2, ChronoUnit.HOURS)
        );
        addReturnCreatedAt(addDeliveredOrder(), ReturnStatus.REQUESTED, now);
        OrderReturn manual = addReturnCreatedAt(
            addDeliveredOrder(), ReturnStatus.REQUESTED, now.minus(2, ChronoUnit.HOURS)
        );
        manual.setManuallyManaged(true);
        addReturnCreatedAt(
            addDeliveredOrder(), ReturnStatus.APPROVED, now.minus(2, ChronoUnit.HOURS)
        );
        flushAndClear();

        List<OrderReturn> found = orderReturnRepository.findByStatusAndCreatedAtBeforeAndManuallyManagedFalse(
            ReturnStatus.REQUESTED, now.minus(1, ChronoUnit.HOURS)
        );

        assertThat(idsOf(found)).containsExactly(match.getId());
    }

    @Test
    @DisplayName("approved before limit: same status, approved before the limit and not manually managed")
    void findApprovedBeforeLimit() {
        Instant now = Instant.now();
        OrderReturn match = addReturn(addDeliveredOrder(), ReturnStatus.APPROVED);
        match.setApprovedAt(now.minus(2, ChronoUnit.HOURS));
        OrderReturn recent = addReturn(addDeliveredOrder(), ReturnStatus.APPROVED);
        recent.setApprovedAt(now);
        OrderReturn manual = addReturn(addDeliveredOrder(), ReturnStatus.APPROVED);
        manual.setApprovedAt(now.minus(2, ChronoUnit.HOURS));
        manual.setManuallyManaged(true);
        flushAndClear();

        List<OrderReturn> found = orderReturnRepository.findByStatusAndApprovedAtBeforeAndManuallyManagedFalse(
            ReturnStatus.APPROVED, now.minus(1, ChronoUnit.HOURS)
        );

        assertThat(idsOf(found)).containsExactly(match.getId());
    }

    @Test
    @DisplayName("received before limit: same status, received before the limit and not manually managed")
    void findReceivedBeforeLimit() {
        Instant now = Instant.now();
        OrderReturn match = addReturn(addDeliveredOrder(), ReturnStatus.RECEIVED);
        match.setReceivedAt(now.minus(2, ChronoUnit.HOURS));
        OrderReturn recent = addReturn(addDeliveredOrder(), ReturnStatus.RECEIVED);
        recent.setReceivedAt(now);
        OrderReturn manual = addReturn(addDeliveredOrder(), ReturnStatus.RECEIVED);
        manual.setReceivedAt(now.minus(2, ChronoUnit.HOURS));
        manual.setManuallyManaged(true);
        flushAndClear();

        List<OrderReturn> found = orderReturnRepository.findByStatusAndReceivedAtBeforeAndManuallyManagedFalse(
            ReturnStatus.RECEIVED, now.minus(1, ChronoUnit.HOURS)
        );

        assertThat(idsOf(found)).containsExactly(match.getId());
    }

    @Test
    @DisplayName("returns of an order: oldest first and only that order")
    void findByOrderIdOrdered() {
        Instant now = Instant.now();
        Order order = addDeliveredOrder();
        OrderReturn newer = addReturnCreatedAt(
            order, ReturnStatus.REQUESTED, now.minus(1, ChronoUnit.HOURS)
        );
        OrderReturn older = addReturnCreatedAt(
            order, ReturnStatus.REJECTED, now.minus(5, ChronoUnit.HOURS)
        );
        addReturnCreatedAt(
            addDeliveredOrder(), ReturnStatus.REQUESTED, now.minus(3, ChronoUnit.HOURS)
        );
        flushAndClear();

        List<OrderReturn> found = orderReturnRepository.findByOrderIdOrderByCreatedAtAscIdAsc(order.getId());

        assertThat(idsOf(found)).containsExactly(older.getId(), newer.getId());
    }

    @Test
    @DisplayName("refund total of an order: sums the refunds of that order in that status, 0 when none")
    void sumRefundAmountByOrderIdAndStatus() {
        Order order = addDeliveredOrder();
        Order other = addDeliveredOrder();
        for (BigDecimal amount : List.of(new BigDecimal("10.50"), new BigDecimal("4.50"))) {
            OrderReturn refunded = addReturn(order, ReturnStatus.REFUNDED);
            refunded.setRefundAmount(amount);
        }
        OrderReturn notRefunded = addReturn(order, ReturnStatus.REQUESTED);
        notRefunded.setRefundAmount(new BigDecimal("99.00"));
        OrderReturn ofOther = addReturn(other, ReturnStatus.REFUNDED);
        ofOther.setRefundAmount(new BigDecimal("7.00"));
        Order untouched = addDeliveredOrder();
        flushAndClear();

        assertThat(orderReturnRepository.sumRefundAmountByOrderIdAndStatus(
            order.getId(), ReturnStatus.REFUNDED
        )).isEqualByComparingTo("15.00");
        assertThat(orderReturnRepository.sumRefundAmountByOrderIdAndStatus(
            untouched.getId(), ReturnStatus.REFUNDED
        )).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("mark manually managed: sets the flag on that return only")
    void markManuallyManaged() {
        OrderReturn target = addReturn(addDeliveredOrder(), ReturnStatus.REQUESTED);
        OrderReturn other = addReturn(addDeliveredOrder(), ReturnStatus.REQUESTED);

        orderReturnRepository.markManuallyManaged(target.getId());

        Boolean targetFlag = jdbcTemplate.queryForObject(
            "select manually_managed from order_returns where id = ?", Boolean.class, target.getId()
        );
        Boolean otherFlag = jdbcTemplate.queryForObject(
            "select manually_managed from order_returns where id = ?", Boolean.class, other.getId()
        );
        assertThat(targetFlag).isTrue();
        assertThat(otherFlag).isFalse();
    }

    @Test
    @DisplayName("paged find all: loads the order and its user with each return and counts every return")
    void pagedFindAllLoadsOrderAndUser() {
        addReturn(addDeliveredOrder(), ReturnStatus.REQUESTED);
        addReturn(addDeliveredOrder(), ReturnStatus.REQUESTED);
        flushAndClear();
        Specification<OrderReturn> everything = (_, _, cb) ->
            cb.conjunction();

        Page<OrderReturn> page = orderReturnRepository.findAll(
            everything, PageRequest.of(0, 1)
        );

        OrderReturn first = page.getContent().getFirst();
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(Hibernate.isInitialized(first.getOrder())).isTrue();
        assertThat(Hibernate.isInitialized(first.getOrder().getUser())).isTrue();
    }

    @Test
    @DisplayName("return spec hasStatus: filters by status and ignores a null status")
    void hasStatus() {
        OrderReturn requested = addReturn(addDeliveredOrder(), ReturnStatus.REQUESTED);
        addReturn(addDeliveredOrder(), ReturnStatus.REFUNDED);
        flushAndClear();

        List<OrderReturn> filtered = orderReturnRepository.findAll(
            ReturnSpecification.hasStatus(ReturnStatus.REQUESTED)
        );
        List<OrderReturn> everything = orderReturnRepository.findAll(ReturnSpecification.hasStatus(null));

        assertThat(idsOf(filtered)).containsExactly(requested.getId());
        assertThat(everything).hasSize(2);
    }

    @Test
    @DisplayName("return spec hasStatusIn: keeps only the given statuses")
    void hasStatusIn() {
        OrderReturn requested = addReturn(addDeliveredOrder(), ReturnStatus.REQUESTED);
        OrderReturn approved = addReturn(addDeliveredOrder(), ReturnStatus.APPROVED);
        addReturn(addDeliveredOrder(), ReturnStatus.REJECTED);
        flushAndClear();

        List<OrderReturn> found = orderReturnRepository.findAll(
            ReturnSpecification.hasStatusIn(ReturnStatus.PENDING_STATUSES)
        );

        assertThat(idsOf(found)).containsExactlyInAnyOrder(requested.getId(), approved.getId());
    }

    @Test
    @DisplayName("return spec orderByRecentDesc: newest return first")
    void orderByRecentDesc() {
        Instant now = Instant.now();
        OrderReturn oldest = addReturnCreatedAt(
            addDeliveredOrder(), ReturnStatus.REQUESTED, now.minus(3, ChronoUnit.DAYS)
        );
        OrderReturn newest = addReturnCreatedAt(
            addDeliveredOrder(), ReturnStatus.REQUESTED, now.minus(1, ChronoUnit.DAYS)
        );
        flushAndClear();

        List<OrderReturn> found = orderReturnRepository.findAll(ReturnSpecification.orderByRecentDesc());

        assertThat(idsOf(found)).containsExactly(newest.getId(), oldest.getId());
    }

    @Test
    @DisplayName("return spec orderByPendingPriority: requested, approved, received, then the rest, oldest first")
    void orderByPendingPriority() {
        Instant now = Instant.now();
        OrderReturn requestedNew = addReturnCreatedAt(
            addDeliveredOrder(), ReturnStatus.REQUESTED, now.minus(1, ChronoUnit.HOURS)
        );
        OrderReturn requestedOld = addReturnCreatedAt(
            addDeliveredOrder(), ReturnStatus.REQUESTED, now.minus(3, ChronoUnit.HOURS)
        );
        OrderReturn approved = addReturnCreatedAt(
            addDeliveredOrder(), ReturnStatus.APPROVED, now.minus(4, ChronoUnit.HOURS)
        );
        OrderReturn received = addReturnCreatedAt(
            addDeliveredOrder(), ReturnStatus.RECEIVED, now.minus(5, ChronoUnit.HOURS)
        );
        OrderReturn rejected = addReturnCreatedAt(
            addDeliveredOrder(), ReturnStatus.REJECTED, now.minus(8, ChronoUnit.HOURS)
        );
        OrderReturn refunded = addReturnCreatedAt(
            addDeliveredOrder(), ReturnStatus.REFUNDED, now.minus(10, ChronoUnit.HOURS)
        );
        flushAndClear();

        List<OrderReturn> found = orderReturnRepository.findAll(ReturnSpecification.orderByPendingPriority());

        assertThat(idsOf(found)).containsExactly(
            requestedOld.getId(), requestedNew.getId(), approved.getId(), received.getId(),
            refunded.getId(), rejected.getId()
        );
    }
}
