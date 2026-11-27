package com.d4rksoul.base.ecommerce.spring.react.persistence;

import com.d4rksoul.base.ecommerce.spring.react.dto.projection.ProductStockProjection;
import com.d4rksoul.base.ecommerce.spring.react.entity.Product;
import com.d4rksoul.base.ecommerce.spring.react.entity.ProductSku;
import com.d4rksoul.base.ecommerce.spring.react.enums.ProductColor;
import com.d4rksoul.base.ecommerce.spring.react.enums.ProductSize;
import com.d4rksoul.base.ecommerce.spring.react.repository.ProductSkuRepository;
import com.d4rksoul.base.ecommerce.spring.react.specification.ProductSkuSpecification;
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

import java.util.List;
import java.util.Optional;

import static com.d4rksoul.base.ecommerce.spring.react.persistence.TestData.sku;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

class ProductSkuRepositoryIT extends AbstractIntegrationTest {

    private static final Pageable FIRST_PAGE = PageRequest.of(0, 10);

    @Autowired
    private ProductSkuRepository productSkuRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Integer stockInDatabase(Long skuId) {
        return jdbcTemplate.queryForObject(
            "select stock from product_skus where id = ?", Integer.class, skuId
        );
    }

    private static List<Long> idsOf(Page<ProductSku> page) {
        return page.getContent().stream().map(ProductSku::getId).toList();
    }

    @Test
    @DisplayName("decrement stock: takes the quantity and reports one updated row")
    void decrementStockTakesTheQuantity() {
        ProductSku sku = addSku(addProduct("A"), ProductSize.M, true, 10);

        int updatedRows = productSkuRepository.decrementStock(sku.getId(), 4);

        assertThat(updatedRows).isEqualTo(1);
        assertThat(stockInDatabase(sku.getId())).isEqualTo(6);
    }

    @Test
    @DisplayName("decrement stock: allows taking exactly what is left")
    void decrementStockAllowsTakingEverything() {
        ProductSku sku = addSku(addProduct("A"), ProductSize.M, true, 3);

        int updatedRows = productSkuRepository.decrementStock(sku.getId(), 3);

        assertThat(updatedRows).isEqualTo(1);
        assertThat(stockInDatabase(sku.getId())).isZero();
    }

    @Test
    @DisplayName("decrement stock: refuses to go below zero and leaves the stock untouched")
    void decrementStockNeverGoesBelowZero() {
        ProductSku sku = addSku(addProduct("A"), ProductSize.M, true, 5);

        int first = productSkuRepository.decrementStock(sku.getId(), 3);
        int second = productSkuRepository.decrementStock(sku.getId(), 3);

        assertThat(first).isEqualTo(1);
        assertThat(second).isZero();
        assertThat(stockInDatabase(sku.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("decrement stock: an unknown sku updates no row")
    void decrementStockOfUnknownSku() {
        assertThat(productSkuRepository.decrementStock(-1L, 1)).isZero();
    }

    @Test
    @DisplayName("decrement stock: an entity loaded before the update is not served stale afterwards")
    void decrementStockRefreshesLoadedEntity() {
        ProductSku sku = addSku(addProduct("A"), ProductSize.M, true, 10);
        Long skuId = sku.getId();

        assertThat(em.find(ProductSku.class, skuId).getStock()).isEqualTo(10);

        productSkuRepository.decrementStock(skuId, 4);

        assertThat(em.find(ProductSku.class, skuId).getStock()).isEqualTo(6);
    }

    @Test
    @DisplayName("increment stock: adds the quantity")
    void incrementStockAddsTheQuantity() {
        ProductSku sku = addSku(addProduct("A"), ProductSize.M, true, 10);

        productSkuRepository.incrementStock(sku.getId(), 5);

        assertThat(stockInDatabase(sku.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("adjust stock: a positive delta adds to the stock")
    void adjustStockWithPositiveDelta() {
        ProductSku sku = addSku(addProduct("A"), ProductSize.M, true, 10);

        int updatedRows = productSkuRepository.adjustStock(sku.getId(), 7);

        assertThat(updatedRows).isEqualTo(1);
        assertThat(stockInDatabase(sku.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("adjust stock: a negative delta takes from the stock")
    void adjustStockWithNegativeDelta() {
        ProductSku sku = addSku(addProduct("A"), ProductSize.M, true, 10);

        int updatedRows = productSkuRepository.adjustStock(sku.getId(), -4);

        assertThat(updatedRows).isEqualTo(1);
        assertThat(stockInDatabase(sku.getId())).isEqualTo(6);
    }

    @Test
    @DisplayName("adjust stock: a negative delta may bring the stock exactly to zero")
    void adjustStockMayReachZero() {
        ProductSku sku = addSku(addProduct("A"), ProductSize.M, true, 4);

        int updatedRows = productSkuRepository.adjustStock(sku.getId(), -4);

        assertThat(updatedRows).isEqualTo(1);
        assertThat(stockInDatabase(sku.getId())).isZero();
    }

    @Test
    @DisplayName("adjust stock: a negative delta that would go below zero changes nothing")
    void adjustStockRefusesNegativeResult() {
        ProductSku sku = addSku(addProduct("A"), ProductSize.M, true, 4);

        int updatedRows = productSkuRepository.adjustStock(sku.getId(), -5);

        assertThat(updatedRows).isZero();
        assertThat(stockInDatabase(sku.getId())).isEqualTo(4);
    }

    @Test
    @DisplayName("set stock: replaces the stock with the given value")
    void setStockReplacesTheValue() {
        ProductSku sku = addSku(addProduct("A"), ProductSize.M, true, 10);

        int updatedRows = productSkuRepository.setStock(sku.getId(), 42);

        assertThat(updatedRows).isEqualTo(1);
        assertThat(stockInDatabase(sku.getId())).isEqualTo(42);
    }

    @Test
    @DisplayName("set stock: an unknown sku updates no row")
    void setStockOfUnknownSku() {
        assertThat(productSkuRepository.setStock(-1L, 5)).isZero();
    }

    @Test
    @DisplayName("stock summary: totals, counts and low stock skus of the active skus of each product")
    void summarizeStockByProductIds() {
        Product first = addProduct("A");
        addSku(first, ProductSize.M, true, 3);
        addSku(first, ProductSize.L, true, 10);
        addSku(first, ProductSize.XL, false, 1);
        Product second = addProduct("B");
        addSku(second, ProductSize.M, true, 5);
        Product onlyInactive = addProduct("C");
        addSku(onlyInactive, ProductSize.M, false, 2);
        flushAndClear();

        List<ProductStockProjection> summary = productSkuRepository.summarizeStockByProductIds(
            List.of(first.getId(), second.getId(), onlyInactive.getId()), 5
        );

        assertThat(summary)
            .extracting(
                ProductStockProjection::productId,
                ProductStockProjection::totalStock,
                ProductStockProjection::skuCount,
                ProductStockProjection::lowStockSkus
            )
            .containsExactlyInAnyOrder(
                tuple(first.getId(), 13L, 2L, 1L),
                tuple(second.getId(), 5L, 1L, 1L)
            );
    }

    @Test
    @DisplayName("find by product: only the skus of that product, ordered by id")
    void findByProductIdOrderByIdAsc() {
        Product product = addProduct("A");
        ProductSku first = addSku(product, ProductSize.M, true, 1);
        ProductSku second = addSku(product, ProductSize.L, true, 1);
        addSku(addProduct("B"), ProductSize.M, true, 1);
        flushAndClear();

        List<ProductSku> found = productSkuRepository.findByProductIdOrderByIdAsc(product.getId());

        assertThat(found).extracting(ProductSku::getId).containsExactly(first.getId(), second.getId());
    }

    @Test
    @DisplayName("find by id and product: the sku must belong to that product")
    void findByIdAndProductId() {
        Product product = addProduct("A");
        ProductSku sku = addSku(product, ProductSize.M, true, 1);
        Product other = addProduct("B");
        flushAndClear();

        assertThat(productSkuRepository.findByIdAndProductId(sku.getId(), product.getId())).isPresent();
        assertThat(productSkuRepository.findByIdAndProductId(sku.getId(), other.getId())).isEmpty();
    }

    @Test
    @DisplayName("find active: returns a sku that is active and belongs to an active product")
    void findActiveSkuOfActiveProduct() {
        ProductSku sku = addSku(addProduct("A"), ProductSize.M, true, 1);
        flushAndClear();

        Optional<ProductSku> found = productSkuRepository
            .findByIdAndActiveTrueAndProductActiveTrue(sku.getId());

        assertThat(found).isPresent();
    }

    @Test
    @DisplayName("find active: an inactive sku is not returned")
    void findActiveIgnoresInactiveSku() {
        ProductSku sku = addSku(addProduct("A"), ProductSize.M, false, 1);
        flushAndClear();

        assertThat(
            productSkuRepository.findByIdAndActiveTrueAndProductActiveTrue(sku.getId())
        ).isEmpty();
    }

    @Test
    @DisplayName("find active: a sku of an inactive product is not returned")
    void findActiveIgnoresInactiveProduct() {
        Product product = addProduct("A");
        ProductSku sku = addSku(product, ProductSize.M, true, 1);
        product.setActive(false);
        flushAndClear();

        assertThat(productSkuRepository.findByIdAndActiveTrueAndProductActiveTrue(sku.getId())).isEmpty();
    }

    @Test
    @DisplayName("find by size and color: matches the exact combination of a product")
    void findByProductIdAndSizeAndColor() {
        Product product = addProduct("A");
        addSku(product, ProductSize.M, true, 1);
        flushAndClear();

        Optional<ProductSku> found = productSkuRepository.findByProductIdAndSizeAndColor(
            product.getId(), ProductSize.M, ProductColor.BLACK
        );
        Optional<ProductSku> otherColor = productSkuRepository.findByProductIdAndSizeAndColor(
            product.getId(), ProductSize.M, ProductColor.BLUE
        );

        assertThat(found).isPresent();
        assertThat(otherColor).isEmpty();
    }

    @Test
    @DisplayName("size and color: the database does not allow the same combination twice in a product")
    void sizeAndColorAreUniquePerProduct() {
        Product product = addProduct("A");
        addSku(product, ProductSize.M, true, 1);
        flushAndClear();

        ProductSku duplicate = sku(em.getReference(Product.class, product.getId()), ProductSize.M);

        assertThatThrownBy(() -> persist(duplicate))
            .isInstanceOf(PersistenceException.class)
            .hasMessageContaining("uk_product_size_color");
    }

    @Test
    @DisplayName("sku spec: the paged query loads the product together with the sku")
    void pagedQueryLoadsTheProduct() {
        addSku(addProduct("A"), ProductSize.M, true, 1);
        flushAndClear();

        Page<ProductSku> page = productSkuRepository.findAll(
            ProductSkuSpecification.isActive(), FIRST_PAGE
        );

        assertThat(page.getContent()).hasSize(1);
        assertThat(Hibernate.isInitialized(page.getContent().getFirst().getProduct())).isTrue();
    }

    @Test
    @DisplayName("sku spec isActive: leaves out inactive skus and skus of inactive products")
    void isActive() {
        ProductSku active = addSku(addProduct("ACTIVE"), ProductSize.M, true, 1);
        addSku(addProduct("INACTIVE_SKU"), ProductSize.M, false, 1);
        Product inactiveProduct = addProduct("INACTIVE_PRODUCT");
        addSku(inactiveProduct, ProductSize.M, true, 1);
        inactiveProduct.setActive(false);
        flushAndClear();

        Page<ProductSku> page = productSkuRepository.findAll(
            ProductSkuSpecification.isActive(), FIRST_PAGE
        );

        assertThat(idsOf(page)).containsExactly(active.getId());
    }

    @Test
    @DisplayName("sku spec hasLowStock: stock at or below the threshold, or everything when disabled")
    void hasLowStock() {
        Product product = addProduct("A");
        ProductSku three = addSku(product, ProductSize.XS, true, 3);
        ProductSku five = addSku(product, ProductSize.S, true, 5);
        addSku(product, ProductSize.M, true, 6);
        flushAndClear();

        Page<ProductSku> low = productSkuRepository.findAll(
            ProductSkuSpecification.hasLowStock(true, 5), FIRST_PAGE
        );
        Page<ProductSku> all = productSkuRepository.findAll(
            ProductSkuSpecification.hasLowStock(false, 5), FIRST_PAGE
        );

        assertThat(idsOf(low)).containsExactlyInAnyOrder(three.getId(), five.getId());
        assertThat(all.getContent()).hasSize(3);
    }

    @Test
    @DisplayName("sku spec containsProductNameOrRef: matches the product name or ref ignoring case")
    void containsProductNameOrRef() {
        Product jacket = addProduct("JKT-1");
        jacket.setName("Rain Jacket");
        ProductSku jacketSku = addSku(jacket, ProductSize.M, true, 1);
        Product tee = addProduct("TEE-2");
        ProductSku teeSku = addSku(tee, ProductSize.M, true, 1);
        flushAndClear();

        Page<ProductSku> byName = productSkuRepository.findAll(
            ProductSkuSpecification.containsProductNameOrRef(" RAIN "), FIRST_PAGE
        );
        Page<ProductSku> byRef = productSkuRepository.findAll(
            ProductSkuSpecification.containsProductNameOrRef("tee-2"), FIRST_PAGE
        );

        assertThat(idsOf(byName)).containsExactly(jacketSku.getId());
        assertThat(idsOf(byRef)).containsExactly(teeSku.getId());
    }

    @Test
    @DisplayName("sku spec orderByStockAsc: lowest stock first, with pagination and total")
    void orderByStockAscWithPagination() {
        Product product = addProduct("A");
        addSku(product, ProductSize.XS, true, 9);
        ProductSku two = addSku(product, ProductSize.S, true, 2);
        ProductSku five = addSku(product, ProductSize.M, true, 5);
        flushAndClear();

        Specification<ProductSku> specification = Specification
            .where(ProductSkuSpecification.orderByStockAsc());
        Page<ProductSku> page = productSkuRepository.findAll(
            specification, PageRequest.of(0, 2)
        );

        assertThat(idsOf(page)).containsExactly(two.getId(), five.getId());
        assertThat(page.getTotalElements()).isEqualTo(3);
    }

    @Test
    @DisplayName("sku spec orderByProductName: alphabetical by product name")
    void orderByProductName() {
        Product zebra = addProduct("Z");
        zebra.setName("Zebra");
        ProductSku zebraSku = addSku(zebra, ProductSize.M, true, 1);
        Product apple = addProduct("A");
        apple.setName("Apple");
        ProductSku appleSku = addSku(apple, ProductSize.M, true, 1);
        flushAndClear();

        Specification<ProductSku> specification = Specification
            .where(ProductSkuSpecification.orderByProductName());
        Page<ProductSku> page = productSkuRepository.findAll(specification, FIRST_PAGE);

        assertThat(idsOf(page)).containsExactly(appleSku.getId(), zebraSku.getId());
    }
}
