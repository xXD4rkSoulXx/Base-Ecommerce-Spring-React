package com.d4rksoul.base.ecommerce.spring.react.integration;

import com.d4rksoul.base.ecommerce.spring.react.dto.projection.ProductCoverProjection;
import com.d4rksoul.base.ecommerce.spring.react.dto.projection.ProductSkuCoverProjection;
import com.d4rksoul.base.ecommerce.spring.react.entity.Product;
import com.d4rksoul.base.ecommerce.spring.react.entity.ProductSku;
import com.d4rksoul.base.ecommerce.spring.react.entity.ProductSkuImage;
import com.d4rksoul.base.ecommerce.spring.react.enums.ProductSize;
import com.d4rksoul.base.ecommerce.spring.react.repository.ProductSkuImageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static com.d4rksoul.base.ecommerce.spring.react.integration.TestData.skuImage;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class ProductSkuImageRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private ProductSkuImageRepository productSkuImageRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ProductSkuImage addImage(
        ProductSku sku, String path, boolean productCover, boolean skuCover
    ) {
        ProductSkuImage image = skuImage(sku, path);
        image.setProductCover(productCover);
        image.setSkuCover(skuCover);

        return persist(image);
    }

    private Boolean productCoverInDatabase(Long imageId) {
        return jdbcTemplate.queryForObject(
            "select product_cover from product_sku_image where id = ?", Boolean.class, imageId
        );
    }

    private Boolean skuCoverInDatabase(Long imageId) {
        return jdbcTemplate.queryForObject(
            "select sku_cover from product_sku_image where id = ?", Boolean.class, imageId
        );
    }

    @Test
    @DisplayName("product covers: the path of the cover image of each requested product")
    void findCoverByProductIds() {
        Product first = addProduct("A");
        ProductSku firstSku = addSku(first, ProductSize.M, true, 1);
        addImage(firstSku, "a-cover.webp", true, true);
        addImage(firstSku, "a-other.webp", false, false);
        Product second = addProduct("B");
        addImage(
            addSku(second, ProductSize.M, true, 1),
            "b-cover.webp",
            true,
            true
        );
        Product notRequested = addProduct("C");
        addImage(
            addSku(notRequested, ProductSize.M, true, 1),
            "c-cover.webp",
            true,
            true
        );
        flushAndClear();

        List<ProductCoverProjection> covers = productSkuImageRepository.findCoverByProductIds(
            List.of(first.getId(), second.getId())
        );

        assertThat(covers)
            .extracting(ProductCoverProjection::productId, ProductCoverProjection::path)
            .containsExactlyInAnyOrder(
                tuple(first.getId(), "a-cover.webp"),
                tuple(second.getId(), "b-cover.webp")
            );
    }

    @Test
    @DisplayName("sku covers: the path of the cover image of each requested sku")
    void findCoversByProductSkuIds() {
        Product product = addProduct("A");
        ProductSku skuM = addSku(product, ProductSize.M, true, 1);
        ProductSku skuL = addSku(product, ProductSize.L, true, 1);
        addImage(skuM, "m-cover.webp", true, true);
        addImage(skuM, "m-other.webp", false, false);
        addImage(skuL, "l-cover.webp", false, true);
        flushAndClear();

        List<ProductSkuCoverProjection> covers = productSkuImageRepository.findCoversByProductSkuIds(
            List.of(skuM.getId(), skuL.getId())
        );

        assertThat(covers)
            .extracting(ProductSkuCoverProjection::productSkuId, ProductSkuCoverProjection::path)
            .containsExactlyInAnyOrder(
                tuple(skuM.getId(), "m-cover.webp"),
                tuple(skuL.getId(), "l-cover.webp")
            );
    }

    @Test
    @DisplayName("images of a sku: only that sku, ordered by id")
    void findByProductSkuIdOrdered() {
        Product product = addProduct("A");
        ProductSku skuM = addSku(product, ProductSize.M, true, 1);
        ProductSku skuL = addSku(product, ProductSize.L, true, 1);
        ProductSkuImage first = addImage(skuM, "1.webp", false, false);
        addImage(skuL, "other.webp", false, false);
        ProductSkuImage second = addImage(skuM, "2.webp", false, false);
        flushAndClear();

        List<ProductSkuImage> images = productSkuImageRepository.findByProductSkuIdOrderByIdAsc(skuM.getId());

        assertThat(images).extracting(ProductSkuImage::getId).containsExactly(first.getId(), second.getId());
    }

    @Test
    @DisplayName("image by id and sku: the image must belong to that sku")
    void findByIdAndProductSkuId() {
        Product product = addProduct("A");
        ProductSku skuM = addSku(product, ProductSize.M, true, 1);
        ProductSku skuL = addSku(product, ProductSize.L, true, 1);
        ProductSkuImage image = addImage(skuM, "1.webp", false, false);
        flushAndClear();

        assertThat(productSkuImageRepository.findByIdAndProductSkuId(image.getId(), skuM.getId()))
            .isPresent();
        assertThat(productSkuImageRepository.findByIdAndProductSkuId(image.getId(), skuL.getId()))
            .isEmpty();
    }

    @Test
    @DisplayName("first image of a sku: the oldest one")
    void findFirstByProductSkuId() {
        ProductSku sku = addSku(addProduct("A"), ProductSize.M, true, 1);
        ProductSkuImage oldest = addImage(sku, "1.webp", false, false);
        addImage(sku, "2.webp", false, false);
        flushAndClear();

        assertThat(productSkuImageRepository.findFirstByProductSkuIdOrderByIdAsc(sku.getId()))
            .get().extracting(ProductSkuImage::getId).isEqualTo(oldest.getId());
    }

    @Test
    @DisplayName("first image of a product: the oldest one across all its skus")
    void findFirstByProductId() {
        Product product = addProduct("A");
        ProductSku skuM = addSku(product, ProductSize.M, true, 1);
        ProductSku skuL = addSku(product, ProductSize.L, true, 1);
        ProductSkuImage oldest = addImage(skuL, "1.webp", false, false);
        addImage(skuM, "2.webp", false, false);
        flushAndClear();

        assertThat(productSkuImageRepository.findFirstByProductSkuProductIdOrderByIdAsc(product.getId()))
            .get().extracting(ProductSkuImage::getId).isEqualTo(oldest.getId());
    }

    @Test
    @DisplayName("exists by sku: true only for a sku that has images")
    void existsByProductSkuId() {
        Product product = addProduct("A");
        ProductSku withImage = addSku(product, ProductSize.M, true, 1);
        ProductSku withoutImage = addSku(product, ProductSize.L, true, 1);
        addImage(withImage, "1.webp", false, false);
        flushAndClear();

        assertThat(productSkuImageRepository.existsByProductSkuId(withImage.getId())).isTrue();
        assertThat(productSkuImageRepository.existsByProductSkuId(withoutImage.getId())).isFalse();
    }

    @Test
    @DisplayName("exists by product: true when any sku of the product has images")
    void existsByProductSkuProductId() {
        Product withImage = addProduct("A");
        addImage(
            addSku(withImage, ProductSize.M, true, 1),
            "1.webp",
            false,
            false
        );
        Product withoutImage = addProduct("B");
        addSku(withoutImage, ProductSize.M, true, 1);
        flushAndClear();

        assertThat(productSkuImageRepository.existsByProductSkuProductId(withImage.getId())).isTrue();
        assertThat(productSkuImageRepository.existsByProductSkuProductId(withoutImage.getId())).isFalse();
    }

    @Test
    @DisplayName("mark product cover: flags only that image")
    void markProductCover() {
        ProductSku sku = addSku(addProduct("A"), ProductSize.M, true, 1);
        ProductSkuImage target = addImage(sku, "1.webp", false, false);
        ProductSkuImage other = addImage(sku, "2.webp", false, false);

        productSkuImageRepository.markProductCover(target.getId());

        assertThat(productCoverInDatabase(target.getId())).isTrue();
        assertThat(productCoverInDatabase(other.getId())).isFalse();
    }

    @Test
    @DisplayName("clear product cover: unflags the covers of every sku of that product only")
    void clearProductCover() {
        Product product = addProduct("A");
        ProductSkuImage coverOfM = addImage(
            addSku(product, ProductSize.M, true, 1),
            "m.webp",
            true,
            false
        );
        Product other = addProduct("B");
        ProductSkuImage coverOfOther = addImage(
            addSku(other, ProductSize.M, true, 1),
            "b.webp",
            true,
            false
        );

        productSkuImageRepository.clearProductCover(product.getId());

        assertThat(productCoverInDatabase(coverOfM.getId())).isFalse();
        assertThat(productCoverInDatabase(coverOfOther.getId())).isTrue();
    }

    @Test
    @DisplayName("mark sku cover: flags only that image")
    void markSkuCover() {
        ProductSku sku = addSku(addProduct("A"), ProductSize.M, true, 1);
        ProductSkuImage target = addImage(sku, "1.webp", false, false);
        ProductSkuImage other = addImage(sku, "2.webp", false, false);

        productSkuImageRepository.markSkuCover(target.getId());

        assertThat(skuCoverInDatabase(target.getId())).isTrue();
        assertThat(skuCoverInDatabase(other.getId())).isFalse();
    }

    @Test
    @DisplayName("clear sku cover: unflags the cover of that sku only")
    void clearSkuCover() {
        Product product = addProduct("A");
        ProductSku skuM = addSku(product, ProductSize.M, true, 1);
        ProductSku skuL = addSku(product, ProductSize.L, true, 1);
        ProductSkuImage coverOfM = addImage(skuM, "m.webp", false, true);
        ProductSkuImage coverOfL = addImage(skuL, "l.webp", false, true);

        productSkuImageRepository.clearSkuCover(skuM.getId());

        assertThat(skuCoverInDatabase(coverOfM.getId())).isFalse();
        assertThat(skuCoverInDatabase(coverOfL.getId())).isTrue();
    }

    @Test
    @DisplayName("changing the product cover: clearing then marking leaves exactly one cover in the product")
    void changingProductCoverLeavesOneCover() {
        Product product = addProduct("A");
        ProductSku skuM = addSku(product, ProductSize.M, true, 1);
        ProductSku skuL = addSku(product, ProductSize.L, true, 1);
        ProductSkuImage oldCover = addImage(skuM, "old.webp", true, true);
        ProductSkuImage newCover = addImage(skuL, "new.webp", false, true);
        productSkuImageRepository.clearProductCover(product.getId());
        productSkuImageRepository.markProductCover(newCover.getId());
        flushAndClear();

        List<ProductCoverProjection> covers = productSkuImageRepository.findCoverByProductIds(
            List.of(product.getId())
        );

        assertThat(covers).extracting(ProductCoverProjection::path).containsExactly("new.webp");
        assertThat(productCoverInDatabase(oldCover.getId())).isFalse();
    }
}
