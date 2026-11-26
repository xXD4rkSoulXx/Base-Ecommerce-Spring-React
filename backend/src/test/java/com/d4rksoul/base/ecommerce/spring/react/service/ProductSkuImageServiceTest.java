package com.d4rksoul.base.ecommerce.spring.react.service;

import com.d4rksoul.base.ecommerce.spring.react.dto.projection.ProductCoverProjection;
import com.d4rksoul.base.ecommerce.spring.react.dto.projection.ProductSkuCoverProjection;
import com.d4rksoul.base.ecommerce.spring.react.entity.Product;
import com.d4rksoul.base.ecommerce.spring.react.entity.ProductSku;
import com.d4rksoul.base.ecommerce.spring.react.repository.ProductSkuImageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductSkuImageServiceTest {

    @Mock
    private ProductSkuImageRepository productSkuImageRepository;

    @InjectMocks
    private ProductSkuImageService service;

    @Test
    @DisplayName("product covers: maps each product to its cover and keeps the first one when there are duplicates")
    void productCoversKeepFirstWhenDuplicated() {
        when(productSkuImageRepository.findCoverByProductIds(List.of(10L, 11L))).thenReturn(List.of(
            new ProductCoverProjection(10L, "first.jpg"),
            new ProductCoverProjection(10L, "second.jpg"),
            new ProductCoverProjection(11L, "other.jpg")
        ));

        Map<Long, String> result = service.findCoversByProductId(List.of(10L, 11L));

        assertThat(result)
            .containsEntry(10L, "first.jpg")
            .containsEntry(11L, "other.jpg")
            .hasSize(2);
    }

    @Test
    @DisplayName("variant covers: the variant's own image wins, otherwise it uses the product's cover")
    void variantCoverFallsBackToProductCover() {
        Product product = Product.builder()
            .id(10L)
            .build();

        ProductSku withOwnCover = ProductSku.builder()
            .id(1L)
            .product(product)
            .build();

        ProductSku withoutOwnCover = ProductSku.builder()
            .id(2L)
            .product(product)
            .build();

        when(productSkuImageRepository.findCoversByProductSkuIds(List.of(1L, 2L)))
            .thenReturn(List.of(new ProductSkuCoverProjection(1L, "variant.jpg")));
        when(productSkuImageRepository.findCoverByProductIds(List.of(10L)))
            .thenReturn(List.of(new ProductCoverProjection(10L, "product.jpg")));

        Map<Long, String> result = service.findCoversBySkuId(List.of(withOwnCover, withoutOwnCover));

        assertThat(result)
            .containsEntry(1L, "variant.jpg")
            .containsEntry(2L, "product.jpg")
            .hasSize(2);
    }

    @Test
    @DisplayName("variant covers: a variant with no image of its own and no product cover is left out")
    void variantWithoutAnyCoverIsLeftOut() {
        Product product = Product.builder()
            .id(11L)
            .build();

        ProductSku sku = ProductSku.builder()
            .id(3L)
            .product(product)
            .build();

        when(productSkuImageRepository.findCoversByProductSkuIds(List.of(3L))).thenReturn(List.of());
        when(productSkuImageRepository.findCoverByProductIds(List.of(11L))).thenReturn(List.of());

        Map<Long, String> result = service.findCoversBySkuId(List.of(sku));

        assertThat(result).isEmpty();
    }
}
