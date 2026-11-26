package com.d4rksoul.base.ecommerce.spring.react.service.admin;

import com.d4rksoul.base.ecommerce.spring.react.dto.request.admin.AdminCreateProductSkuRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.admin.AdminUpdateProductSkuRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminProductSkuResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.result.PackingItemResult;
import com.d4rksoul.base.ecommerce.spring.react.entity.Product;
import com.d4rksoul.base.ecommerce.spring.react.entity.ProductSku;
import com.d4rksoul.base.ecommerce.spring.react.enums.AuditAction;
import com.d4rksoul.base.ecommerce.spring.react.enums.ProductCategory;
import com.d4rksoul.base.ecommerce.spring.react.enums.ProductColor;
import com.d4rksoul.base.ecommerce.spring.react.enums.ProductSize;
import com.d4rksoul.base.ecommerce.spring.react.exception.InvalidProductSkuException;
import com.d4rksoul.base.ecommerce.spring.react.exception.ProductNotFoundException;
import com.d4rksoul.base.ecommerce.spring.react.exception.ProductSkuAlreadyExistsException;
import com.d4rksoul.base.ecommerce.spring.react.exception.ProductSkuNotFoundException;
import com.d4rksoul.base.ecommerce.spring.react.mapper.ShippingMapper;
import com.d4rksoul.base.ecommerce.spring.react.mapper.admin.AdminProductSkuMapper;
import com.d4rksoul.base.ecommerce.spring.react.repository.ProductRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.ProductSkuRepository;
import com.d4rksoul.base.ecommerce.spring.react.service.ParcelPackingService;
import com.d4rksoul.base.ecommerce.spring.react.service.ProductSkuImageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminProductSkuServiceTest {

    private static final long PRODUCT_ID = 1L;
    private static final long SKU_ID = 5L;

    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductSkuRepository productSkuRepository;
    @Mock
    private ProductSkuImageService productSkuImageService;
    @Mock
    private ParcelPackingService parcelPackingService;
    @Mock
    private AdminAuditLogService adminAuditLogService;
    @Mock
    private AdminProductSkuMapper adminProductSkuMapper;
    @Mock
    private ShippingMapper shippingMapper;

    @InjectMocks
    private AdminProductSkuService service;

    private final AdminCreateProductSkuRequest createRequest = new AdminCreateProductSkuRequest(
        ProductSize.M,
        ProductColor.BLACK,
        500,
        30,
        20,
        5,
        0
    );
    private final AdminUpdateProductSkuRequest updateRequest = new AdminUpdateProductSkuRequest(
        800,40, 30, 10, 15
    );
    private final PackingItemResult packingItem = PackingItemResult.builder()
        .weightKg(0.5)
        .lengthCm(30)
        .widthCm(20)
        .heightCm(5)
        .build();

    private final ProductSku sku = ProductSku.builder()
        .id(SKU_ID)
        .weightGrams(100)
        .lengthCm(10)
        .widthCm(10)
        .heightCm(10)
        .discountPercentage(0)
        .build();

    @Test
    @DisplayName("create: reports a product that does not exist")
    void createReportsUnknownProduct() {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createSku(PRODUCT_ID, createRequest))
            .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    @DisplayName("create: rejects a size that the product category does not allow")
    void createRejectsSizeNotAllowedForCategory() {
        when(productRepository.findById(PRODUCT_ID))
            .thenReturn(Optional.of(product(ProductCategory.FOOTWEAR)));

        assertThatThrownBy(() -> service.createSku(PRODUCT_ID, createRequest))
            .isInstanceOf(InvalidProductSkuException.class)
            .hasMessageContaining("Size M is not available for FOOTWEAR");

        verify(productSkuRepository, never()).save(any(ProductSku.class));
    }

    @Test
    @DisplayName("create: rejects a variant with the same size and color")
    void createRejectsDuplicateVariant() {
        ProductSku existing = ProductSku.builder()
            .active(true)
            .build();

        when(productRepository.findById(PRODUCT_ID))
            .thenReturn(Optional.of(product(ProductCategory.TOPS)));
        when(productSkuRepository.findByProductIdAndSizeAndColor(
            PRODUCT_ID, ProductSize.M, ProductColor.BLACK
        )).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.createSku(PRODUCT_ID, createRequest))
            .isInstanceOf(ProductSkuAlreadyExistsException.class)
            .hasMessageContaining("already has a M BLACK variant");

        verify(productSkuRepository, never()).save(any(ProductSku.class));
    }

    @Test
    @DisplayName("create: when the duplicate is archived, the message tells the admin to restore it")
    void createSuggestsRestoringArchivedVariant() {
        ProductSku archived = ProductSku.builder()
            .active(false)
            .build();

        when(productRepository.findById(PRODUCT_ID))
            .thenReturn(Optional.of(product(ProductCategory.TOPS)));
        when(productSkuRepository.findByProductIdAndSizeAndColor(
            PRODUCT_ID, ProductSize.M, ProductColor.BLACK
        )).thenReturn(Optional.of(archived));

        assertThatThrownBy(() -> service.createSku(PRODUCT_ID, createRequest))
            .isInstanceOf(ProductSkuAlreadyExistsException.class)
            .hasMessageContaining("archived")
            .hasMessageContaining("Restore it instead");
    }

    @Test
    @DisplayName("create: rejects a variant whose weight and size do not fit any shipping box")
    void createRejectsVariantThatDoesNotFitAnyBox() {
        IllegalStateException failure = new IllegalStateException("No box fits");
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product(ProductCategory.TOPS)));
        when(productSkuRepository.findByProductIdAndSizeAndColor(
            PRODUCT_ID, ProductSize.M, ProductColor.BLACK
        )).thenReturn(Optional.empty());
        when(shippingMapper.toPackingItem(createRequest)).thenReturn(packingItem);
        when(parcelPackingService.pack(List.of(packingItem))).thenThrow(failure);

        assertThatThrownBy(() -> service.createSku(PRODUCT_ID, createRequest)).isSameAs(failure);

        verify(productSkuRepository, never()).save(any(ProductSku.class));
    }

    @Test
    @DisplayName("create: saves the variant, registers it and returns it with its cover")
    void createSavesVariantAndRegistersIt() {
        Product product = product(ProductCategory.TOPS);
        AdminProductSkuResponse response = mock(AdminProductSkuResponse.class);

        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(productSkuRepository.findByProductIdAndSizeAndColor(
            PRODUCT_ID, ProductSize.M, ProductColor.BLACK
        )).thenReturn(Optional.empty());
        when(shippingMapper.toPackingItem(createRequest)).thenReturn(packingItem);
        when(adminProductSkuMapper.toEntity(product, createRequest)).thenReturn(sku);
        when(productSkuImageService.findCoversBySkuId(List.of(sku)))
            .thenReturn(Map.of(SKU_ID, "cover.jpg"));
        when(adminProductSkuMapper.toDto(sku, "cover.jpg")).thenReturn(response);

        AdminProductSkuResponse result = service.createSku(PRODUCT_ID, createRequest);

        assertThat(result).isSameAs(response);

        verify(parcelPackingService).pack(List.of(packingItem));
        verify(productSkuRepository).save(sku);
        verify(adminAuditLogService).registerVariant(AuditAction.CREATED, sku, null);
    }

    @Test
    @DisplayName("update: reports a variant that does not exist in that product")
    void updateReportsUnknownVariant() {
        when(productSkuRepository.findByIdAndProductId(SKU_ID, PRODUCT_ID))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateSku(PRODUCT_ID, SKU_ID, updateRequest))
            .isInstanceOf(ProductSkuNotFoundException.class);
    }

    @Test
    @DisplayName("update: changes the weight, dimensions and discount and registers it")
    void updateChangesFieldsAndRegistersIt() {
        AdminProductSkuResponse response = mock(AdminProductSkuResponse.class);

        when(productSkuRepository.findByIdAndProductId(SKU_ID, PRODUCT_ID))
            .thenReturn(Optional.of(sku));
        when(shippingMapper.toPackingItem(updateRequest)).thenReturn(packingItem);
        when(productSkuImageService.findCoversBySkuId(List.of(sku))).thenReturn(Map.of());
        when(adminProductSkuMapper.toDto(sku, null)).thenReturn(response);

        AdminProductSkuResponse result = service.updateSku(PRODUCT_ID, SKU_ID, updateRequest);

        assertThat(result).isSameAs(response);
        assertThat(sku.getWeightGrams()).isEqualTo(800);
        assertThat(sku.getLengthCm()).isEqualTo(40);
        assertThat(sku.getWidthCm()).isEqualTo(30);
        assertThat(sku.getHeightCm()).isEqualTo(10);
        assertThat(sku.getDiscountPercentage()).isEqualTo(15);

        verify(adminAuditLogService).registerVariant(AuditAction.UPDATED, sku, null);
    }

    @Test
    @DisplayName("update: if the new measures do not fit any shipping box, nothing is changed")
    void updateRejectsMeasuresThatDoNotFitAnyBox() {
        IllegalStateException failure = new IllegalStateException("No box fits");
        when(productSkuRepository.findByIdAndProductId(SKU_ID, PRODUCT_ID))
            .thenReturn(Optional.of(sku));
        when(shippingMapper.toPackingItem(updateRequest)).thenReturn(packingItem);
        when(parcelPackingService.pack(List.of(packingItem))).thenThrow(failure);

        assertThatThrownBy(() -> service.updateSku(PRODUCT_ID, SKU_ID, updateRequest))
            .isSameAs(failure);

        assertThat(sku.getWeightGrams()).isEqualTo(100);

        verify(adminAuditLogService, never()).registerVariant(any(), any(), any());
    }

    @Test
    @DisplayName("archive: deactivates the variant and registers it")
    void archiveDeactivatesVariant() {
        sku.setActive(true);

        when(productSkuRepository.findByIdAndProductId(SKU_ID, PRODUCT_ID))
            .thenReturn(Optional.of(sku));

        service.archiveSku(PRODUCT_ID, SKU_ID);

        assertThat(sku.isActive()).isFalse();

        verify(adminAuditLogService).registerVariant(AuditAction.ARCHIVED, sku, null);
    }

    @Test
    @DisplayName("restore: reactivates the variant and registers it")
    void restoreReactivatesVariant() {
        sku.setActive(false);

        when(productSkuRepository.findByIdAndProductId(SKU_ID, PRODUCT_ID))
            .thenReturn(Optional.of(sku));

        service.restoreSku(PRODUCT_ID, SKU_ID);

        assertThat(sku.isActive()).isTrue();

        verify(adminAuditLogService).registerVariant(AuditAction.RESTORED, sku, null);
    }

    @Test
    @DisplayName("archive: reports a variant that does not exist in that product")
    void archiveReportsUnknownVariant() {
        when(productSkuRepository.findByIdAndProductId(SKU_ID, PRODUCT_ID))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.archiveSku(PRODUCT_ID, SKU_ID))
            .isInstanceOf(ProductSkuNotFoundException.class);
    }

    @Test
    @DisplayName("list: reports a product that does not exist")
    void listReportsUnknownProduct() {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getSkus(PRODUCT_ID))
            .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    @DisplayName("list: returns the variants of the product, each with its cover when it has one")
    void listReturnsVariantsWithCovers() {
        ProductSku other = ProductSku.builder()
            .id(6L)
            .build();

        AdminProductSkuResponse first = mock(AdminProductSkuResponse.class);
        AdminProductSkuResponse second = mock(AdminProductSkuResponse.class);

        when(productRepository.findById(PRODUCT_ID))
            .thenReturn(Optional.of(product(ProductCategory.TOPS)));
        when(productSkuRepository.findByProductIdOrderByIdAsc(PRODUCT_ID))
            .thenReturn(List.of(sku, other));
        when(productSkuImageService.findCoversBySkuId(List.of(sku, other)))
            .thenReturn(Map.of(SKU_ID, "cover.jpg"));
        when(adminProductSkuMapper.toDto(sku, "cover.jpg")).thenReturn(first);
        when(adminProductSkuMapper.toDto(other, null)).thenReturn(second);

        List<AdminProductSkuResponse> result = service.getSkus(PRODUCT_ID);

        assertThat(result).containsExactly(first, second);
    }

    private Product product(ProductCategory category) {
        return Product.builder().id(PRODUCT_ID).category(category).build();
    }
}
