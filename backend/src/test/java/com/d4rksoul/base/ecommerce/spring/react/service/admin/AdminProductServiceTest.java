package com.d4rksoul.base.ecommerce.spring.react.service.admin;

import com.d4rksoul.base.ecommerce.spring.react.constant.Constants;
import com.d4rksoul.base.ecommerce.spring.react.dto.projection.ProductStockProjection;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.admin.AdminProductRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.admin.AdminProductSearchRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminProductDetailsResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminProductSummaryResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.Product;
import com.d4rksoul.base.ecommerce.spring.react.enums.AuditAction;
import com.d4rksoul.base.ecommerce.spring.react.enums.AuditEntityType;
import com.d4rksoul.base.ecommerce.spring.react.enums.FeaturedSlot;
import com.d4rksoul.base.ecommerce.spring.react.enums.ProductCategory;
import com.d4rksoul.base.ecommerce.spring.react.exception.FeaturedSlotTakenException;
import com.d4rksoul.base.ecommerce.spring.react.exception.ProductNotFoundException;
import com.d4rksoul.base.ecommerce.spring.react.exception.ProductRefAlreadyExistsException;
import com.d4rksoul.base.ecommerce.spring.react.mapper.PageMapper;
import com.d4rksoul.base.ecommerce.spring.react.mapper.admin.AdminProductMapper;
import com.d4rksoul.base.ecommerce.spring.react.repository.ProductRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.ProductSkuRepository;
import com.d4rksoul.base.ecommerce.spring.react.service.ProductSkuImageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminProductServiceTest {

    private static final long PRODUCT_ID = 1L;

    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductSkuRepository productSkuRepository;
    @Mock
    private ProductSkuImageService productSkuImageService;
    @Mock
    private AdminAuditLogService adminAuditLogService;
    @Mock
    private AdminProductMapper adminProductMapper;
    @Mock
    private PageMapper pageMapper;

    @Captor
    private ArgumentCaptor<Page<AdminProductSummaryResponse>> pageCaptor;

    @InjectMocks
    private AdminProductService service;

    private final Product product = Product.builder()
        .id(PRODUCT_ID)
        .ref("OLD-REF")
        .name("Jacket")
        .build();

    @Test
    @DisplayName("create: rejects a ref that already exists")
    void createRejectsExistingRef() {
        AdminProductRequest request = request(null);

        when(productRepository.existsByRef("NEW-1")).thenReturn(true);

        assertThatThrownBy(() -> service.createProduct(request))
            .isInstanceOf(ProductRefAlreadyExistsException.class);

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("create: rejects a featured slot that is held by another product")
    void createRejectsTakenFeaturedSlot() {
        AdminProductRequest request = request(FeaturedSlot.LEFT);

        Product holder = Product.builder()
            .id(9L)
            .name("Coat")
            .build();

        when(productRepository.existsByRef("NEW-1")).thenReturn(false);
        when(productRepository.findByFeaturedSlot(FeaturedSlot.LEFT))
            .thenReturn(Optional.of(holder));

        assertThatThrownBy(() -> service.createProduct(request))
            .isInstanceOf(FeaturedSlotTakenException.class)
            .hasMessageContaining("left slot")
            .hasMessageContaining("Coat");

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("create: saves the product and registers it in the audit log")
    void createSavesProductAndRegistersIt() {
        AdminProductRequest request = request(null);
        AdminProductDetailsResponse response = mock(AdminProductDetailsResponse.class);

        Product created = Product.builder()
            .id(PRODUCT_ID)
            .ref("NEW-1")
            .build();

        when(productRepository.existsByRef("NEW-1")).thenReturn(false);
        when(adminProductMapper.toEntity(request)).thenReturn(created);
        when(adminProductMapper.toDetailsDto(created)).thenReturn(response);

        AdminProductDetailsResponse result = service.createProduct(request);

        assertThat(result).isSameAs(response);
        verify(productRepository).save(created);
        verify(adminAuditLogService).register(
            AuditAction.CREATED, AuditEntityType.PRODUCT, PRODUCT_ID, "NEW-1"
        );
    }

    @Test
    @DisplayName("update: reports a product that does not exist")
    void updateReportsUnknownProduct() {
        AdminProductRequest request = request(null);

        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateProduct(PRODUCT_ID, request))
            .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    @DisplayName("update: rejects a ref that belongs to another product")
    void updateRejectsRefOfAnotherProduct() {
        AdminProductRequest request = request(null);

        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(productRepository.existsByRefAndIdNot("NEW-1", PRODUCT_ID)).thenReturn(true);

        assertThatThrownBy(() -> service.updateProduct(PRODUCT_ID, request))
            .isInstanceOf(ProductRefAlreadyExistsException.class);

        assertThat(product.getRef()).isEqualTo("OLD-REF");
    }

    @Test
    @DisplayName("update: rejects a featured slot that is held by another product")
    void updateRejectsTakenFeaturedSlot() {
        AdminProductRequest request = request(FeaturedSlot.RIGHT);

        Product holder = Product.builder()
            .id(9L)
            .name("Coat")
            .build();

        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(productRepository.existsByRefAndIdNot("NEW-1", PRODUCT_ID)).thenReturn(false);
        when(productRepository.findByFeaturedSlot(FeaturedSlot.RIGHT)).thenReturn(Optional.of(holder));

        assertThatThrownBy(() -> service.updateProduct(PRODUCT_ID, request))
            .isInstanceOf(FeaturedSlotTakenException.class)
            .hasMessageContaining("right slot");

        assertThat(product.getRef()).isEqualTo("OLD-REF");
    }

    @Test
    @DisplayName("update: a product can keep its own featured slot and all its fields are changed")
    void updateChangesFieldsAndKeepsOwnFeaturedSlot() {
        AdminProductRequest request = request(FeaturedSlot.LEFT);
        AdminProductDetailsResponse response = mock(AdminProductDetailsResponse.class);

        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(productRepository.existsByRefAndIdNot("NEW-1", PRODUCT_ID)).thenReturn(false);
        when(productRepository.findByFeaturedSlot(FeaturedSlot.LEFT)).thenReturn(Optional.of(product));
        when(adminProductMapper.toDetailsDto(product)).thenReturn(response);

        AdminProductDetailsResponse result = service.updateProduct(PRODUCT_ID, request);

        assertThat(result).isSameAs(response);
        assertThat(product.getRef()).isEqualTo("NEW-1");
        assertThat(product.getName()).isEqualTo("Coat");
        assertThat(product.getDescription()).isEqualTo("Warm");
        assertThat(product.getPrice()).isEqualByComparingTo("50.00");
        assertThat(product.getDiscountPercentage()).isEqualTo(10);
        assertThat(product.getCategory()).isEqualTo(ProductCategory.OUTERWEAR);
        assertThat(product.getFeaturedSlot()).isEqualTo(FeaturedSlot.LEFT);

        verify(adminAuditLogService).register(
            AuditAction.UPDATED, AuditEntityType.PRODUCT, PRODUCT_ID, "NEW-1"
        );
    }

    @Test
    @DisplayName("archive: deactivates the product, removes it from the featured slot and registers it")
    void archiveDeactivatesProductAndClearsFeaturedSlot() {
        product.setActive(true);
        product.setFeaturedSlot(FeaturedSlot.LEFT);

        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));

        service.archiveProduct(PRODUCT_ID);

        assertThat(product.isActive()).isFalse();
        assertThat(product.getFeaturedSlot()).isNull();
        verify(adminAuditLogService)
            .register(AuditAction.ARCHIVED, AuditEntityType.PRODUCT, PRODUCT_ID, "OLD-REF");
    }

    @Test
    @DisplayName("restore: reactivates the product and registers it")
    void restoreReactivatesProduct() {
        product.setActive(false);

        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));

        service.restoreProduct(PRODUCT_ID);

        assertThat(product.isActive()).isTrue();

        verify(adminAuditLogService).register(
            AuditAction.RESTORED, AuditEntityType.PRODUCT, PRODUCT_ID, "OLD-REF"
        );
    }

    @Test
    @DisplayName("archive: reports a product that does not exist")
    void archiveReportsUnknownProduct() {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.archiveProduct(PRODUCT_ID))
            .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    @DisplayName("list: joins each product with its cover and stock, using empty stock when it has no variants")
    void listJoinsCoverAndStock() {
        Product first = Product.builder()
            .id(1L)
            .build();

        Product second = Product.builder()
            .id(2L)
            .build();

        ProductStockProjection firstStock = new ProductStockProjection(
            1L, 30L, 3L, 1L
        );

        AdminProductSummaryResponse firstSummary = mock(AdminProductSummaryResponse.class);
        AdminProductSummaryResponse secondSummary = mock(AdminProductSummaryResponse.class);

        when(productRepository.findAll(
            ArgumentMatchers.<Specification<Product>>any(),
            eq(PageRequest.of(0, 10))
        )).thenReturn(new PageImpl<>(List.of(first, second)));
        when(productSkuImageService.findCoversByProductId(List.of(1L, 2L)))
            .thenReturn(Map.of(1L, "cover.jpg"));
        when(productSkuRepository.summarizeStockByProductIds(List.of(1L, 2L), Constants.LOW_STOCK_THRESHOLD))
            .thenReturn(List.of(firstStock));
        when(adminProductMapper.toSummaryDto(first, "cover.jpg", firstStock)).thenReturn(firstSummary);
        when(adminProductMapper.toSummaryDto(second, null, ProductStockProjection.empty(2L)))
            .thenReturn(secondSummary);

        service.getProducts(new AdminProductSearchRequest(
            null, null, null, null, null
        ));

        verify(pageMapper).toDto(pageCaptor.capture());

        assertThat(pageCaptor.getValue().getContent()).containsExactly(firstSummary, secondSummary);
    }

    private AdminProductRequest request(FeaturedSlot slot) {
        return new AdminProductRequest(
            "NEW-1",
            "Coat",
            "Warm",
            new BigDecimal("50.00"),
            10,
            ProductCategory.OUTERWEAR,
            slot
        );
    }
}
