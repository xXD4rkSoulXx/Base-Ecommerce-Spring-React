package com.d4rksoul.base.ecommerce.spring.react.integration;

import com.d4rksoul.base.ecommerce.spring.react.entity.Order;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderItem;
import com.d4rksoul.base.ecommerce.spring.react.entity.Product;
import com.d4rksoul.base.ecommerce.spring.react.entity.ProductSku;
import com.d4rksoul.base.ecommerce.spring.react.entity.User;
import com.d4rksoul.base.ecommerce.spring.react.enums.OrderStatus;
import com.d4rksoul.base.ecommerce.spring.react.enums.ProductSize;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static com.d4rksoul.base.ecommerce.spring.react.integration.TestData.order;
import static com.d4rksoul.base.ecommerce.spring.react.integration.TestData.orderItem;
import static com.d4rksoul.base.ecommerce.spring.react.integration.TestData.product;
import static com.d4rksoul.base.ecommerce.spring.react.integration.TestData.sku;
import static com.d4rksoul.base.ecommerce.spring.react.integration.TestData.user;
import static org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest(showSql = false)
@AutoConfigureTestDatabase(replace = NONE)
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=validate")
public abstract class AbstractIntegrationTest {

    private static final PostgreSQLContainer POSTGRES =
        new PostgreSQLContainer("postgres:17-alpine");

    static {
        POSTGRES.start();
    }

    @Autowired
    protected EntityManager em;

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    protected <T> T persist(T entity) {
        em.persist(entity);
        em.flush();

        return entity;
    }

    protected void flushAndClear() {
        em.flush();
        em.clear();
    }

    protected Product addProduct(String ref) {
        return persist(product(ref));
    }

    protected ProductSku addSku(Product product, ProductSize size, boolean active, int stock) {
        ProductSku sku = sku(product, size);
        sku.setActive(active);
        sku.setStock(stock);
        persist(sku);
        product.getProductSkus().add(sku);

        return sku;
    }

    protected Product addVisibleProduct(String ref) {
        Product product = addProduct(ref);
        addSku(product, ProductSize.M, true, 10);

        return product;
    }

    protected User addUser() {
        return persist(user());
    }

    protected Order addOrder(User user, OrderStatus status) {
        return persist(order(user, status));
    }

    protected OrderItem addOrderItem(Order order, ProductSku sku, int quantity) {
        return persist(orderItem(order, sku, quantity));
    }
}
