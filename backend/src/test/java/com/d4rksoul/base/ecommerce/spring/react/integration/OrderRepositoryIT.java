package com.d4rksoul.base.ecommerce.spring.react.integration;

import com.d4rksoul.base.ecommerce.spring.react.entity.DiscountCode;
import com.d4rksoul.base.ecommerce.spring.react.entity.Order;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderItem;
import com.d4rksoul.base.ecommerce.spring.react.entity.Product;
import com.d4rksoul.base.ecommerce.spring.react.entity.ProductSku;
import com.d4rksoul.base.ecommerce.spring.react.entity.User;
import com.d4rksoul.base.ecommerce.spring.react.enums.OrderStatus;
import com.d4rksoul.base.ecommerce.spring.react.enums.ShipmentStatus;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderRepository;
import com.d4rksoul.base.ecommerce.spring.react.specification.OrderSpecification;
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
import java.util.Optional;

import static com.d4rksoul.base.ecommerce.spring.react.integration.TestData.discountCode;
import static com.d4rksoul.base.ecommerce.spring.react.integration.TestData.order;
import static com.d4rksoul.base.ecommerce.spring.react.integration.TestData.orderItem;
import static com.d4rksoul.base.ecommerce.spring.react.integration.TestData.shipment;
import static com.d4rksoul.base.ecommerce.spring.react.integration.TestData.user;
import static org.assertj.core.api.Assertions.assertThat;

class OrderRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Order addOrder(OrderStatus status, Instant paidAt) {
        Order order = order(addUser(), status);
        order.setPaidAt(paidAt);

        return persist(order);
    }

    private Order addOrderCreatedAt(OrderStatus status, Instant createdAt) {
        Order order = addOrder(addUser(), status);
        setCreatedAt(order, createdAt);

        return order;
    }

    private void addShipments(Order order, ShipmentStatus... statuses) {
        for (ShipmentStatus status : statuses) {
            persist(shipment(order, status));
        }
    }

    private void addItemNamed(Order order, Product product, String productName) {
        OrderItem item = orderItem(order, product.getProductSkus().getFirst(), 1);
        item.setProductName(productName);
        persist(item);
    }

    private Order addOrderOfCustomer(String email, String recipientName) {
        User customer = user();
        customer.setEmail(email);
        persist(customer);
        Order order = order(customer, OrderStatus.PAID);
        order.setRecipientName(recipientName);

        return persist(order);
    }

    private void setCreatedAt(Order order, Instant createdAt) {
        jdbcTemplate.update(
            "update orders set created_at = ? where id = ?",
            Timestamp.from(createdAt),
            order.getId()
        );
    }

    private String statusInDatabase(Long orderId) {
        return jdbcTemplate.queryForObject(
            "select status from orders where id = ?",
            String.class,
            orderId
        );
    }

    private static List<Long> idsOf(List<Order> orders) {
        return orders.stream().map(Order::getId).toList();
    }

    private List<Long> search(Specification<Order> specification) {
        return idsOf(orderRepository.findAll(specification));
    }

    @Test
    @DisplayName("update status: changes the status when the current one is the expected one")
    void updateStatusWhenCurrentStatusMatches() {
        Order order = addOrder(OrderStatus.PAID, Instant.now());

        int updatedRows = orderRepository.updateStatus(
            order.getId(), OrderStatus.PAID, OrderStatus.PROCESSING
        );

        assertThat(updatedRows).isEqualTo(1);
        assertThat(statusInDatabase(order.getId())).isEqualTo("PROCESSING");
    }

    @Test
    @DisplayName("update status: does nothing when the current status is not the expected one")
    void updateStatusWhenCurrentStatusDiffers() {
        Order order = addOrder(OrderStatus.SHIPPED, Instant.now());

        int updatedRows = orderRepository.updateStatus(
            order.getId(), OrderStatus.PAID, OrderStatus.PROCESSING
        );

        assertThat(updatedRows).isZero();
        assertThat(statusInDatabase(order.getId())).isEqualTo("SHIPPED");
    }

    @Test
    @DisplayName("update status: an unknown order updates no row")
    void updateStatusOfUnknownOrder() {
        int updatedRows = orderRepository.updateStatus(-1L, OrderStatus.PAID, OrderStatus.PROCESSING);

        assertThat(updatedRows).isZero();
    }

    @Test
    @DisplayName("mark manually managed: sets the flag on that order only")
    void markManuallyManaged() {
        Order target = addOrder(OrderStatus.PAID, Instant.now());
        Order other = addOrder(OrderStatus.PAID, Instant.now());

        orderRepository.markManuallyManaged(target.getId());

        Boolean targetFlag = jdbcTemplate.queryForObject(
            "select manually_managed from orders where id = ?", Boolean.class, target.getId()
        );
        Boolean otherFlag = jdbcTemplate.queryForObject(
            "select manually_managed from orders where id = ?", Boolean.class, other.getId()
        );

        assertThat(targetFlag).isTrue();
        assertThat(otherFlag).isFalse();
    }

    @Test
    @DisplayName("find by checkout session: matches the Stripe session id")
    void findByStripeCheckoutSessionId() {
        Order order = order(addUser(), OrderStatus.PENDING);
        order.setStripeCheckoutSessionId("cs_1");
        persist(order);
        flushAndClear();

        assertThat(orderRepository.findByStripeCheckoutSessionId("cs_1")).isPresent();
        assertThat(orderRepository.findByStripeCheckoutSessionId("cs_other")).isEmpty();
    }

    @Test
    @DisplayName("find by user and status: only the orders of that user with that status")
    void findByUserIdAndStatus() {
        User user = addUser();
        Order pending = addOrder(user, OrderStatus.PENDING);
        addOrder(user, OrderStatus.PAID);
        addOrder(OrderStatus.PENDING, null);
        flushAndClear();

        List<Order> found = orderRepository.findByUserIdAndStatus(user.getId(), OrderStatus.PENDING);

        assertThat(idsOf(found)).containsExactly(pending.getId());
    }

    @Test
    @DisplayName("find by id and status: the current status must be one of the given ones")
    void findByIdAndStatusIn() {
        Order order = addOrder(OrderStatus.PAID, Instant.now());
        flushAndClear();

        Optional<Order> inStatuses = orderRepository.findByIdAndStatusIn(
            order.getId(), List.of(OrderStatus.PAID, OrderStatus.PROCESSING)
        );
        Optional<Order> notInStatuses = orderRepository.findByIdAndStatusIn(
            order.getId(), List.of(OrderStatus.DELIVERED)
        );

        assertThat(inStatuses).isPresent();
        assertThat(notInStatuses).isEmpty();
    }

    @Test
    @DisplayName("find with details: loads discount code, items, skus and products of the user's order")
    void findByIdAndUserIdAndStatusInLoadsTheGraph() {
        User user = addUser();
        DiscountCode code = persist(discountCode("SAVE10"));
        Order order = order(user, OrderStatus.PAID);
        order.setDiscountCode(code);
        persist(order);
        Product product = addVisibleProduct("A");
        OrderItem item = addOrderItem(order, product.getProductSkus().getFirst(), 2);
        flushAndClear();

        Order found = orderRepository.findByIdAndUserIdAndStatusIn(
            order.getId(), user.getId(), List.of(OrderStatus.PAID)
        ).orElseThrow();

        ProductSku foundSku = found.getOrderItems().getFirst().getProductSku();

        assertThat(found.getOrderItems()).extracting(OrderItem::getId).containsExactly(item.getId());
        assertThat(Hibernate.isInitialized(found.getDiscountCode())).isTrue();
        assertThat(Hibernate.isInitialized(foundSku)).isTrue();
        assertThat(Hibernate.isInitialized(foundSku.getProduct())).isTrue();
    }

    @Test
    @DisplayName("find with details: another user or another status finds nothing")
    void findByIdAndUserIdAndStatusInRespectsOwnerAndStatus() {
        User owner = addUser();
        User stranger = addUser();
        Order order = addOrder(owner, OrderStatus.PAID);
        flushAndClear();

        Optional<Order> ofStranger = orderRepository.findByIdAndUserIdAndStatusIn(
            order.getId(), stranger.getId(), List.of(OrderStatus.PAID)
        );
        Optional<Order> otherStatus = orderRepository.findByIdAndUserIdAndStatusIn(
            order.getId(), owner.getId(), List.of(OrderStatus.DELIVERED)
        );

        assertThat(ofStranger).isEmpty();
        assertThat(otherStatus).isEmpty();
    }

    @Test
    @DisplayName("expired checkouts: pending orders with a session created before the limit")
    void findExpiredCheckouts() {
        Instant now = Instant.now();
        Order expired = order(addUser(), OrderStatus.PENDING);
        expired.setStripeCheckoutSessionId("cs_expired");
        persist(expired);
        Order recent = order(addUser(), OrderStatus.PENDING);
        recent.setStripeCheckoutSessionId("cs_recent");
        persist(recent);
        Order withoutSession = addOrder(OrderStatus.PENDING, null);
        Order paid = order(addUser(), OrderStatus.PAID);
        paid.setStripeCheckoutSessionId("cs_paid");
        persist(paid);
        for (Order oldOrder : List.of(expired, withoutSession, paid)) {
            setCreatedAt(oldOrder, now.minus(2, ChronoUnit.HOURS));
        }
        flushAndClear();

        List<Order> found = orderRepository.findByStatusAndStripeCheckoutSessionIdNotNullAndCreatedAtBefore(
            OrderStatus.PENDING, now.minus(1, ChronoUnit.HOURS)
        );

        assertThat(idsOf(found)).containsExactly(expired.getId());
    }

    @Test
    @DisplayName("paid before limit: same status, paid before the limit and not manually managed")
    void findByStatusAndPaidAtBefore() {
        Instant now = Instant.now();
        Instant twoHoursAgo = now.minus(2, ChronoUnit.HOURS);
        Order match = addOrder(OrderStatus.PAID, twoHoursAgo);
        addOrder(OrderStatus.PAID, now);
        Order manual = addOrder(OrderStatus.PAID, twoHoursAgo);
        manual.setManuallyManaged(true);
        addOrder(OrderStatus.PROCESSING, twoHoursAgo);
        flushAndClear();

        List<Order> found = orderRepository.findByStatusAndPaidAtBeforeAndManuallyManagedFalse(
            OrderStatus.PAID, now.minus(1, ChronoUnit.HOURS)
        );

        assertThat(idsOf(found)).containsExactly(match.getId());
    }

    @Test
    @DisplayName("paid before limit without shipments: leaves out orders that already have a shipment")
    void findByStatusAndPaidAtBeforeWithoutShipments() {
        Instant now = Instant.now();
        Instant twoHoursAgo = now.minus(2, ChronoUnit.HOURS);
        Order withoutShipments = addOrder(OrderStatus.PAID, twoHoursAgo);
        Order withShipment = addOrder(OrderStatus.PAID, twoHoursAgo);
        addShipments(withShipment, ShipmentStatus.PRE_TRANSIT);
        Order manual = addOrder(OrderStatus.PAID, twoHoursAgo);
        manual.setManuallyManaged(true);
        addOrder(OrderStatus.PAID, now);
        flushAndClear();

        List<Order> found = orderRepository.findByStatusAndPaidAtBeforeWithoutShipments(
            OrderStatus.PAID, now.minus(1, ChronoUnit.HOURS)
        );

        assertThat(idsOf(found)).containsExactly(withoutShipments.getId());
    }

    @Test
    @DisplayName("any shipment other than: needs a shipment with a different status, once per order")
    void findWithAnyShipmentOtherThan() {
        Order advanced = addOrder(OrderStatus.PROCESSING, Instant.now());
        addShipments(advanced, ShipmentStatus.IN_TRANSIT, ShipmentStatus.DELIVERED);
        Order notAdvanced = addOrder(OrderStatus.PROCESSING, Instant.now());
        addShipments(notAdvanced, ShipmentStatus.PRE_TRANSIT);
        addOrder(OrderStatus.PROCESSING, Instant.now());
        Order manual = addOrder(OrderStatus.PROCESSING, Instant.now());
        manual.setManuallyManaged(true);
        addShipments(manual, ShipmentStatus.IN_TRANSIT);
        Order otherStatus = addOrder(OrderStatus.PAID, Instant.now());
        addShipments(otherStatus, ShipmentStatus.IN_TRANSIT);
        flushAndClear();

        List<Order> found = orderRepository.findWithAnyShipmentOtherThan(
            OrderStatus.PROCESSING, ShipmentStatus.PRE_TRANSIT
        );

        assertThat(idsOf(found)).containsExactly(advanced.getId());
    }

    @Test
    @DisplayName("all shipments in: needs at least one shipment and every shipment in that status")
    void findWithAllShipmentsIn() {
        Order allDelivered = addOrder(OrderStatus.SHIPPED, Instant.now());
        addShipments(allDelivered, ShipmentStatus.DELIVERED, ShipmentStatus.DELIVERED);
        Order mixed = addOrder(OrderStatus.SHIPPED, Instant.now());
        addShipments(mixed, ShipmentStatus.DELIVERED, ShipmentStatus.IN_TRANSIT);
        addOrder(OrderStatus.SHIPPED, Instant.now());
        Order manual = addOrder(OrderStatus.SHIPPED, Instant.now());
        manual.setManuallyManaged(true);
        addShipments(manual, ShipmentStatus.DELIVERED);
        Order otherStatus = addOrder(OrderStatus.PAID, Instant.now());
        addShipments(otherStatus, ShipmentStatus.DELIVERED);
        flushAndClear();

        List<Order> found = orderRepository.findWithAllShipmentsIn(
            OrderStatus.SHIPPED, ShipmentStatus.DELIVERED
        );

        assertThat(idsOf(found)).containsExactly(allDelivered.getId());
    }

    @Test
    @DisplayName("user purchased product: true only for that user, product and status")
    void existsUserPurchasedProduct() {
        User buyer = addUser();
        User stranger = addUser();
        Product bought = addVisibleProduct("BOUGHT");
        Product other = addVisibleProduct("OTHER");
        Order order = addOrder(buyer, OrderStatus.DELIVERED);
        addOrderItem(order, bought.getProductSkus().getFirst(), 1);
        flushAndClear();

        assertThat(orderRepository.existsUserPurchasedProduct(
            buyer.getId(), bought.getId(), OrderStatus.DELIVERED)).isTrue();
        assertThat(orderRepository.existsUserPurchasedProduct(
            buyer.getId(), other.getId(), OrderStatus.DELIVERED)).isFalse();
        assertThat(orderRepository.existsUserPurchasedProduct(
            stranger.getId(), bought.getId(), OrderStatus.DELIVERED)).isFalse();
        assertThat(orderRepository.existsUserPurchasedProduct(
            buyer.getId(), bought.getId(), OrderStatus.PAID)).isFalse();
    }

    @Test
    @DisplayName("discount already used: ignores the orders in statuses where the code was not consumed")
    void existsByUserIdAndDiscountCodeIdAndStatusNotIn() {
        User user = addUser();
        User stranger = addUser();
        DiscountCode used = persist(discountCode("USED"));
        DiscountCode released = persist(discountCode("RELEASED"));
        Order paid = order(user, OrderStatus.PAID);
        paid.setDiscountCode(used);
        persist(paid);
        Order canceled = order(user, OrderStatus.CANCELED);
        canceled.setDiscountCode(released);
        persist(canceled);
        flushAndClear();

        assertThat(orderRepository.existsByUserIdAndDiscountCodeIdAndStatusNotIn(
            user.getId(), used.getId(), OrderStatus.DISCOUNT_UNUSED_STATUSES)).isTrue();
        assertThat(orderRepository.existsByUserIdAndDiscountCodeIdAndStatusNotIn(
            user.getId(), released.getId(), OrderStatus.DISCOUNT_UNUSED_STATUSES)).isFalse();
        assertThat(orderRepository.existsByUserIdAndDiscountCodeIdAndStatusNotIn(
            stranger.getId(), used.getId(), OrderStatus.DISCOUNT_UNUSED_STATUSES)).isFalse();
    }

    @Test
    @DisplayName("paged find all: loads the user with each order and counts every order")
    void pagedFindAllLoadsTheUser() {
        addOrder(OrderStatus.PAID, Instant.now());
        addOrder(OrderStatus.PAID, Instant.now());
        flushAndClear();
        Specification<Order> everything = (_, _, cb) ->
            cb.conjunction();

        Page<Order> page = orderRepository.findAll(everything, PageRequest.of(0, 1));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(Hibernate.isInitialized(page.getContent().getFirst().getUser())).isTrue();
    }

    @Test
    @DisplayName("spec search: matches the product name of an item ignoring case")
    void searchMatchesItemProductName() {
        Order jacket = addOrder(addUser(), OrderStatus.PAID);
        addItemNamed(jacket, addVisibleProduct("JKT-1"), "Rain Jacket");
        Order tee = addOrder(addUser(), OrderStatus.PAID);
        addItemNamed(tee, addVisibleProduct("TEE-2"), "Basic Tee");
        flushAndClear();

        assertThat(search(OrderSpecification.matchesSearch(" RAIN ")))
            .containsExactly(jacket.getId());
    }

    @Test
    @DisplayName("spec search: matches the ref of the product of an item")
    void searchMatchesProductRef() {
        Order jacket = addOrder(addUser(), OrderStatus.PAID);
        addItemNamed(jacket, addVisibleProduct("JKT-1"), "Rain Jacket");
        Order tee = addOrder(addUser(), OrderStatus.PAID);
        addItemNamed(tee, addVisibleProduct("TEE-2"), "Basic Tee");
        flushAndClear();

        assertThat(search(OrderSpecification.matchesSearch("tee-2"))).containsExactly(tee.getId());
    }

    @Test
    @DisplayName("spec search: finds an order by its id, with or without the # prefix")
    void searchMatchesOrderId() {
        Order first = addOrder(addUser(), OrderStatus.PAID);
        Order second = addOrder(addUser(), OrderStatus.PAID);
        flushAndClear();

        assertThat(search(OrderSpecification.matchesSearch("#" + first.getId())))
            .containsExactly(first.getId());
        assertThat(search(OrderSpecification.matchesSearch(String.valueOf(second.getId()))))
            .containsExactly(second.getId());
    }

    @Test
    @DisplayName("spec search: a number too long to be an id is only searched as text")
    void searchWithTooLongNumberDoesNotFail() {
        addOrder(addUser(), OrderStatus.PAID);
        flushAndClear();

        assertThat(search(OrderSpecification.matchesSearch("1234567890123456789012"))).isEmpty();
    }

    @Test
    @DisplayName("spec search: a blank text does not filter, in the order and in the customer search")
    void blankSearchDoesNotFilter() {
        addOrder(addUser(), OrderStatus.PAID);
        addOrder(addUser(), OrderStatus.PAID);
        flushAndClear();

        assertThat(search(OrderSpecification.matchesSearch("  "))).hasSize(2);
        assertThat(search(OrderSpecification.matchesSearch(null))).hasSize(2);
        assertThat(search(OrderSpecification.matchesCustomerSearch(" "))).hasSize(2);
        assertThat(search(OrderSpecification.matchesCustomerSearch(null))).hasSize(2);
    }

    @Test
    @DisplayName("spec customer search: matches the email of the user ignoring case")
    void customerSearchMatchesEmail() {
        Order ana = addOrderOfCustomer("ana@mail.com", "Ana Costa");
        addOrderOfCustomer("rui@mail.com", "Rui Lopes");
        flushAndClear();

        assertThat(search(OrderSpecification.matchesCustomerSearch("ANA@")))
            .containsExactly(ana.getId());
    }

    @Test
    @DisplayName("spec customer search: matches the recipient name")
    void customerSearchMatchesRecipientName() {
        addOrderOfCustomer("ana@mail.com", "Ana Costa");
        Order rui = addOrderOfCustomer("rui@mail.com", "Rui Lopes");
        flushAndClear();

        assertThat(search(OrderSpecification.matchesCustomerSearch("lopes")))
            .containsExactly(rui.getId());
    }

    @Test
    @DisplayName("spec customer search: finds an order by its id")
    void customerSearchMatchesOrderId() {
        Order ana = addOrderOfCustomer("ana@mail.com", "Ana Costa");
        addOrderOfCustomer("rui@mail.com", "Rui Lopes");
        flushAndClear();

        assertThat(search(OrderSpecification.matchesCustomerSearch("#" + ana.getId())))
            .containsExactly(ana.getId());
    }

    @Test
    @DisplayName("spec hasStatus: filters by status and ignores a null status")
    void hasStatus() {
        Order paid = addOrder(addUser(), OrderStatus.PAID);
        addOrder(addUser(), OrderStatus.SHIPPED);
        flushAndClear();

        assertThat(search(OrderSpecification.hasStatus(OrderStatus.PAID)))
            .containsExactly(paid.getId());
        assertThat(search(OrderSpecification.hasStatus(null))).hasSize(2);
    }

    @Test
    @DisplayName("spec hasStatusIn: keeps only the given statuses")
    void hasStatusIn() {
        Order paid = addOrder(addUser(), OrderStatus.PAID);
        Order processing = addOrder(addUser(), OrderStatus.PROCESSING);
        addOrder(addUser(), OrderStatus.SHIPPED);
        flushAndClear();

        List<Long> found = search(
            OrderSpecification.hasStatusIn(List.of(OrderStatus.PAID, OrderStatus.PROCESSING))
        );

        assertThat(found).containsExactlyInAnyOrder(paid.getId(), processing.getId());
    }

    @Test
    @DisplayName("spec belongsToUser: only the orders of that user")
    void belongsToUser() {
        User owner = addUser();
        Order mine = addOrder(owner, OrderStatus.PAID);
        addOrder(addUser(), OrderStatus.PAID);
        flushAndClear();

        assertThat(search(OrderSpecification.belongsToUser(owner.getId()))).containsExactly(mine.getId());
    }

    @Test
    @DisplayName("spec createdFrom and createdTo: restrict the creation date and ignore null bounds")
    void createdFromAndTo() {
        Instant now = Instant.now();
        Order threeDaysAgo = addOrderCreatedAt(
            OrderStatus.PAID, now.minus(3, ChronoUnit.DAYS)
        );
        Order twoDaysAgo = addOrderCreatedAt(
            OrderStatus.PAID, now.minus(2, ChronoUnit.DAYS)
        );
        Order yesterday = addOrderCreatedAt(
            OrderStatus.PAID, now.minus(1, ChronoUnit.DAYS)
        );
        flushAndClear();

        Instant from = now.minus(60, ChronoUnit.HOURS);
        Instant to = now.minus(36, ChronoUnit.HOURS);

        assertThat(search(OrderSpecification.createdFrom(from)))
            .containsExactlyInAnyOrder(twoDaysAgo.getId(), yesterday.getId());
        assertThat(search(OrderSpecification.createdTo(to)))
            .containsExactlyInAnyOrder(threeDaysAgo.getId(), twoDaysAgo.getId());
        assertThat(search(Specification.where(OrderSpecification.createdFrom(from))
            .and(OrderSpecification.createdTo(to)))).containsExactly(twoDaysAgo.getId());
        assertThat(search(Specification.where(OrderSpecification.createdFrom(null))
            .and(OrderSpecification.createdTo(null)))).hasSize(3);
    }

    @Test
    @DisplayName("spec orderByRecentDesc: newest order first")
    void orderByRecentDesc() {
        Instant now = Instant.now();
        Order oldest = addOrderCreatedAt(OrderStatus.PAID, now.minus(3, ChronoUnit.DAYS));
        Order newest = addOrderCreatedAt(OrderStatus.PAID, now.minus(1, ChronoUnit.DAYS));
        Order middle = addOrderCreatedAt(OrderStatus.PAID, now.minus(2, ChronoUnit.DAYS));
        flushAndClear();

        List<Long> found = search(OrderSpecification.orderByRecentDesc());

        assertThat(found).containsExactly(newest.getId(), middle.getId(), oldest.getId());
    }

    @Test
    @DisplayName("spec orderByFulfillmentPriority: paid first, then processing, then the rest, oldest first")
    void orderByFulfillmentPriority() {
        Instant now = Instant.now();
        Order paidNew = addOrderCreatedAt(OrderStatus.PAID, now.minus(1, ChronoUnit.HOURS));
        Order paidOld = addOrderCreatedAt(OrderStatus.PAID, now.minus(3, ChronoUnit.HOURS));
        Order processing = addOrderCreatedAt(
            OrderStatus.PROCESSING, now.minus(5, ChronoUnit.HOURS)
        );
        Order shipped = addOrderCreatedAt(
            OrderStatus.SHIPPED, now.minus(10, ChronoUnit.HOURS)
        );
        Order delivered = addOrderCreatedAt(
            OrderStatus.DELIVERED, now.minus(20, ChronoUnit.HOURS)
        );
        flushAndClear();

        List<Long> found = search(OrderSpecification.orderByFulfillmentPriority());

        assertThat(found).containsExactly(
            paidOld.getId(), paidNew.getId(), processing.getId(), delivered.getId(), shipped.getId()
        );
    }

    @Test
    @DisplayName("spec search with ordering and pagination: the total counts every match")
    void searchWithPaginationCountsEveryMatch() {
        Instant now = Instant.now();
        Product product = addVisibleProduct("JKT-1");
        Order newest = addOrderCreatedAt(OrderStatus.PAID, now.minus(1, ChronoUnit.HOURS));
        Order middle = addOrderCreatedAt(OrderStatus.PAID, now.minus(2, ChronoUnit.HOURS));
        Order oldest = addOrderCreatedAt(OrderStatus.PAID, now.minus(3, ChronoUnit.HOURS));
        Order unrelated = addOrderCreatedAt(
            OrderStatus.PAID, now.minus(4, ChronoUnit.HOURS)
        );
        for (Order order : List.of(newest, middle, oldest)) {
            addItemNamed(order, product, "Rain Jacket");
        }
        addItemNamed(unrelated, addVisibleProduct("TEE-2"), "Basic Tee");
        flushAndClear();
        Specification<Order> specification = Specification
            .where(OrderSpecification.matchesSearch("jacket"))
            .and(OrderSpecification.orderByRecentDesc());

        Page<Order> page = orderRepository.findAll(specification, PageRequest.of(0, 2));

        assertThat(page.getContent()).extracting(Order::getId)
            .containsExactly(newest.getId(), middle.getId());
        assertThat(page.getTotalElements()).isEqualTo(3);
    }
}
