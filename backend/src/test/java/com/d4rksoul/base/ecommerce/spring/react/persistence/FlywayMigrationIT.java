package com.d4rksoul.base.ecommerce.spring.react.persistence;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class FlywayMigrationIT extends AbstractIntegrationTest {

    private static final List<String> EXPECTED_TABLES = List.of(
        "audit_logs", "cart_items", "carts", "discount_codes", "order_items", "order_return_items",
        "order_returns", "order_shipment_items", "order_shipments", "order_status_events", "orders",
        "product_sku_image", "product_skus", "products", "refresh_tokens", "reviews",
        "shipment_tracking_events", "users", "wishlist_items", "wishlists"
    );

    @Autowired
    private Flyway flyway;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Environment environment;

    @Test
    @DisplayName("flyway: every migration is applied and none is pending")
    void everyMigrationIsApplied() {
        assertThat(flyway.info().applied()).isNotEmpty();
        assertThat(flyway.info().pending()).isEmpty();
    }

    @Test
    @DisplayName("flyway: no migration is recorded as failed")
    void noMigrationFailed() {
        Integer failed = jdbcTemplate.queryForObject(
            "select count(*) from flyway_schema_history where success = false", Integer.class
        );

        assertThat(failed).isZero();
    }

    @Test
    @DisplayName("flyway: the applied migrations match the files (checksums)")
    void migrationsPassValidation() {
        assertThatCode(flyway::validate).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("flyway: the migrations create the table of every entity")
    void createsEveryEntityTable() {
        List<String> tables = jdbcTemplate.queryForList(
            """
                select table_name from information_schema.tables
                where table_schema = 'public' and table_name <> 'flyway_schema_history'
                """,
            String.class
        );

        assertThat(tables).containsExactlyInAnyOrderElementsOf(EXPECTED_TABLES);
    }

    @Test
    @DisplayName("hibernate: validates the schema instead of changing it")
    void hibernateOnlyValidatesTheSchema() {
        assertThat(environment.getProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("validate");
    }
}
