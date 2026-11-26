package com.d4rksoul.base.ecommerce.spring.react.service.admin;

import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminProductSkuImageResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.ProductSku;
import com.d4rksoul.base.ecommerce.spring.react.entity.ProductSkuImage;
import com.d4rksoul.base.ecommerce.spring.react.enums.AuditAction;
import com.d4rksoul.base.ecommerce.spring.react.enums.ImageFormat;
import com.d4rksoul.base.ecommerce.spring.react.exception.InvalidImageException;
import com.d4rksoul.base.ecommerce.spring.react.exception.ProductSkuImageNotFoundException;
import com.d4rksoul.base.ecommerce.spring.react.exception.ProductSkuNotFoundException;
import com.d4rksoul.base.ecommerce.spring.react.mapper.admin.AdminProductSkuImageMapper;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderItemRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.ProductSkuImageRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.ProductSkuRepository;
import com.d4rksoul.base.ecommerce.spring.react.storage.ImageOptimizer;
import com.d4rksoul.base.ecommerce.spring.react.storage.ImageStorage;
import com.d4rksoul.base.ecommerce.spring.react.storage.OptimizedImage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminProductSkuImageServiceTest {

    private static final long PRODUCT_ID = 1L;
    private static final long SKU_ID = 5L;
    private static final long IMAGE_ID = 7L;
    private static final String PATH = "/uploads/a.jpg";
    private static final byte[] JPEG_BYTES = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00};

    @Mock
    private ProductSkuRepository productSkuRepository;
    @Mock
    private ProductSkuImageRepository productSkuImageRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private ImageStorage imageStorage;
    @Mock
    private ImageOptimizer imageOptimizer;
    @Mock
    private AdminAuditLogService adminAuditLogService;
    @Mock
    private AdminProductSkuImageMapper adminProductSkuImageMapper;

    @InjectMocks
    private AdminProductSkuImageService service;

    private final ProductSku sku = ProductSku.builder()
        .id(SKU_ID)
        .build();

    private final ProductSkuImage image = ProductSkuImage.builder()
        .id(IMAGE_ID)
        .path(PATH)
        .build();

    @BeforeEach
    void setUp() {
        TransactionSynchronizationManager.initSynchronization();
    }

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.clearSynchronization();
    }

    static Stream<Arguments> acceptedImages() {
        return Stream.of(
            arguments(JPEG_BYTES, ImageFormat.JPEG),
            arguments(
                new byte[]{(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n', 0x00},
                ImageFormat.PNG
            ),
            arguments("RIFF....WEBP".getBytes(StandardCharsets.US_ASCII), ImageFormat.WEBP)
        );
    }

    @Test
    @DisplayName("upload: reports a variant that does not exist in that product")
    void uploadReportsUnknownVariant() {
        MockMultipartFile jpeg = file(JPEG_BYTES);

        when(productSkuRepository.findByIdAndProductId(SKU_ID, PRODUCT_ID))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.uploadImage(PRODUCT_ID, SKU_ID, jpeg))
            .isInstanceOf(ProductSkuNotFoundException.class);
    }

    @Test
    @DisplayName("upload: rejects an empty file")
    void uploadRejectsEmptyFile() {
        MockMultipartFile emptyFile = file(new byte[0]);

        when(productSkuRepository.findByIdAndProductId(SKU_ID, PRODUCT_ID))
            .thenReturn(Optional.of(sku));

        assertThatThrownBy(() -> service.uploadImage(PRODUCT_ID, SKU_ID, emptyFile))
            .isInstanceOf(InvalidImageException.class)
            .hasMessageContaining("Select an image");

        verifyNoInteractions(imageOptimizer, imageStorage);
    }

    @Test
    @DisplayName("upload: rejects a file that is not a JPEG, PNG or WebP, whatever its name says")
    void uploadRejectsUnsupportedFormat() {
        MockMultipartFile gif = file("GIF89a-not-accepted".getBytes(StandardCharsets.US_ASCII));

        when(productSkuRepository.findByIdAndProductId(SKU_ID, PRODUCT_ID)).thenReturn(Optional.of(sku));

        assertThatThrownBy(() -> service.uploadImage(PRODUCT_ID, SKU_ID, gif))
            .isInstanceOf(InvalidImageException.class)
            .hasMessageContaining("Only JPEG, PNG and WebP");

        verifyNoInteractions(imageOptimizer, imageStorage);
    }

    @ParameterizedTest(name = "{1} is detected from the first bytes")
    @MethodSource("acceptedImages")
    @DisplayName("upload: detects the format from the file signature and saves the image without cover flags")
    void uploadDetectsFormatFromSignature(byte[] content, ImageFormat expectedFormat) {
        OptimizedImage optimized = new OptimizedImage(new byte[]{1}, ImageFormat.JPEG);

        when(productSkuRepository.findByIdAndProductId(SKU_ID, PRODUCT_ID)).thenReturn(Optional.of(sku));
        when(imageOptimizer.optimize(content, expectedFormat)).thenReturn(optimized);
        when(imageStorage.store(optimized.content(), "jpg")).thenReturn(PATH);
        when(productSkuImageRepository.existsByProductSkuId(SKU_ID)).thenReturn(true);
        when(productSkuImageRepository.existsByProductSkuProductId(PRODUCT_ID)).thenReturn(true);
        when(adminProductSkuImageMapper.toEntity(sku, PATH, false, false))
            .thenReturn(image);

        service.uploadImage(PRODUCT_ID, SKU_ID, file(content));

        verify(productSkuImageRepository).save(image);
        verify(adminAuditLogService).registerVariant(AuditAction.IMAGE_UPLOADED, sku, null);
    }

    @Test
    @DisplayName("upload: the first image of a variant and of a product becomes both covers")
    void uploadFirstImageBecomesBothCovers() {
        AdminProductSkuImageResponse response = mock(AdminProductSkuImageResponse.class);

        givenFirstJpegUpload();

        when(adminProductSkuImageMapper.toDto(image)).thenReturn(response);

        AdminProductSkuImageResponse result = service.uploadImage(PRODUCT_ID, SKU_ID, file(JPEG_BYTES));

        assertThat(result).isSameAs(response);

        verify(productSkuImageRepository).save(image);
    }

    @Test
    @DisplayName("upload: if the transaction rolls back, the file that was already stored is deleted")
    void uploadDeletesStoredFileOnRollback() {
        givenFirstJpegUpload();

        service.uploadImage(PRODUCT_ID, SKU_ID, file(JPEG_BYTES));
        for (
            TransactionSynchronization synchronization
                : TransactionSynchronizationManager.getSynchronizations()
        ) {
            synchronization.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);
        }

        verify(imageStorage).delete(PATH);
    }

    @Test
    @DisplayName("upload: if the transaction commits, the stored file is kept")
    void uploadKeepsStoredFileOnCommit() {
        givenFirstJpegUpload();

        service.uploadImage(PRODUCT_ID, SKU_ID, file(JPEG_BYTES));
        for (
            TransactionSynchronization synchronization
                : TransactionSynchronizationManager.getSynchronizations()
        ) {
            synchronization.afterCompletion(TransactionSynchronization.STATUS_COMMITTED);
        }

        verify(imageStorage, never()).delete(any());
    }

    @Test
    @DisplayName("list: returns the images of the variant")
    void listReturnsImagesOfVariant() {
        AdminProductSkuImageResponse response = mock(AdminProductSkuImageResponse.class);

        when(productSkuRepository.findByIdAndProductId(SKU_ID, PRODUCT_ID))
            .thenReturn(Optional.of(sku));
        when(productSkuImageRepository.findByProductSkuIdOrderByIdAsc(SKU_ID))
            .thenReturn(List.of(image));
        when(adminProductSkuImageMapper.toDto(image)).thenReturn(response);

        List<AdminProductSkuImageResponse> result = service.getImages(PRODUCT_ID, SKU_ID);

        assertThat(result).containsExactly(response);
    }

    @Test
    @DisplayName("product cover: replaces the previous cover and registers the change")
    void setProductCoverReplacesPreviousCover() {
        when(productSkuRepository.findByIdAndProductId(SKU_ID, PRODUCT_ID))
            .thenReturn(Optional.of(sku));
        when(productSkuImageRepository.findByIdAndProductSkuId(IMAGE_ID, SKU_ID))
            .thenReturn(Optional.of(image));

        service.setProductCover(PRODUCT_ID, SKU_ID, IMAGE_ID);

        verify(productSkuImageRepository).clearProductCover(PRODUCT_ID);
        verify(productSkuImageRepository).markProductCover(IMAGE_ID);
        verify(adminAuditLogService).registerVariant(
            AuditAction.COVER_CHANGED, sku, "Product cover"
        );
    }

    @Test
    @DisplayName("variant cover: replaces the previous cover and registers the change")
    void setSkuCoverReplacesPreviousCover() {
        when(productSkuRepository.findByIdAndProductId(SKU_ID, PRODUCT_ID))
            .thenReturn(Optional.of(sku));
        when(productSkuImageRepository.findByIdAndProductSkuId(IMAGE_ID, SKU_ID))
            .thenReturn(Optional.of(image));

        service.setSkuCover(PRODUCT_ID, SKU_ID, IMAGE_ID);

        verify(productSkuImageRepository).clearSkuCover(SKU_ID);
        verify(productSkuImageRepository).markSkuCover(IMAGE_ID);
        verify(adminAuditLogService).registerVariant(
            AuditAction.COVER_CHANGED, sku, "Variant cover"
        );
    }

    @Test
    @DisplayName("cover: an image that does not belong to the variant cannot become a cover")
    void setCoverRejectsImageOfAnotherVariant() {
        when(productSkuRepository.findByIdAndProductId(SKU_ID, PRODUCT_ID))
            .thenReturn(Optional.of(sku));
        when(productSkuImageRepository.findByIdAndProductSkuId(IMAGE_ID, SKU_ID))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.setSkuCover(PRODUCT_ID, SKU_ID, IMAGE_ID))
            .isInstanceOf(ProductSkuImageNotFoundException.class);

        verify(productSkuImageRepository, never()).clearSkuCover(any());
    }

    @Test
    @DisplayName("delete: reports a variant that does not exist in that product")
    void deleteReportsUnknownVariant() {
        when(productSkuRepository.findByIdAndProductId(SKU_ID, PRODUCT_ID))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteImage(PRODUCT_ID, SKU_ID, IMAGE_ID))
            .isInstanceOf(ProductSkuNotFoundException.class);
    }

    @Test
    @DisplayName("delete: reports an image that does not belong to the variant")
    void deleteReportsUnknownImage() {
        when(productSkuRepository.findByIdAndProductId(SKU_ID, PRODUCT_ID))
            .thenReturn(Optional.of(sku));
        when(productSkuImageRepository.findByIdAndProductSkuId(IMAGE_ID, SKU_ID))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteImage(PRODUCT_ID, SKU_ID, IMAGE_ID))
            .isInstanceOf(ProductSkuImageNotFoundException.class);

        verify(productSkuImageRepository, never()).delete(any());
    }

    @Test
    @DisplayName("delete: when the deleted image was a cover, the next image of the variant and product take over")
    void deleteMovesCoversToNextImages() {
        ProductSkuImage cover = ProductSkuImage.builder()
            .id(IMAGE_ID)
            .path(PATH)
            .skuCover(true)
            .productCover(true)
            .build();

        ProductSkuImage nextOfVariant = ProductSkuImage.builder()
            .id(8L)
            .build();

        ProductSkuImage nextOfProduct = ProductSkuImage.builder()
            .id(9L)
            .build();

        when(productSkuRepository.findByIdAndProductId(SKU_ID, PRODUCT_ID))
            .thenReturn(Optional.of(sku));
        when(productSkuImageRepository.findByIdAndProductSkuId(IMAGE_ID, SKU_ID))
            .thenReturn(Optional.of(cover));
        when(productSkuImageRepository.findFirstByProductSkuIdOrderByIdAsc(SKU_ID))
            .thenReturn(Optional.of(nextOfVariant));
        when(productSkuImageRepository.findFirstByProductSkuProductIdOrderByIdAsc(PRODUCT_ID))
            .thenReturn(Optional.of(nextOfProduct));

        service.deleteImage(PRODUCT_ID, SKU_ID, IMAGE_ID);

        verify(productSkuImageRepository).delete(cover);
        verify(productSkuImageRepository).markSkuCover(8L);
        verify(productSkuImageRepository).markProductCover(9L);
        verify(adminAuditLogService).registerVariant(AuditAction.IMAGE_DELETED, sku, null);
    }

    @Test
    @DisplayName("delete: an image that is not a cover does not change any cover")
    void deleteOfNormalImageKeepsCovers() {
        when(productSkuRepository.findByIdAndProductId(SKU_ID, PRODUCT_ID))
            .thenReturn(Optional.of(sku));
        when(productSkuImageRepository.findByIdAndProductSkuId(IMAGE_ID, SKU_ID))
            .thenReturn(Optional.of(image));

        service.deleteImage(PRODUCT_ID, SKU_ID, IMAGE_ID);

        verify(productSkuImageRepository, never()).markSkuCover(any());
        verify(productSkuImageRepository, never()).markProductCover(any());
    }

    @Test
    @DisplayName("delete: the file is removed from storage only after the transaction commits")
    void deleteRemovesFileAfterCommit() {
        when(productSkuRepository.findByIdAndProductId(SKU_ID, PRODUCT_ID))
            .thenReturn(Optional.of(sku));
        when(productSkuImageRepository.findByIdAndProductSkuId(IMAGE_ID, SKU_ID))
            .thenReturn(Optional.of(image));
        when(orderItemRepository.existsByImagePath(PATH)).thenReturn(false);

        service.deleteImage(PRODUCT_ID, SKU_ID, IMAGE_ID);

        verify(imageStorage, never()).delete(any());
        for (
            TransactionSynchronization synchronization
                : TransactionSynchronizationManager.getSynchronizations()
        ) {
            synchronization.afterCommit();
        }
        verify(imageStorage).delete(PATH);
    }

    @Test
    @DisplayName("delete: the file is kept in storage when a past order still shows that image")
    void deleteKeepsFileUsedByOrders() {
        when(productSkuRepository.findByIdAndProductId(SKU_ID, PRODUCT_ID))
            .thenReturn(Optional.of(sku));
        when(productSkuImageRepository.findByIdAndProductSkuId(IMAGE_ID, SKU_ID))
            .thenReturn(Optional.of(image));
        when(orderItemRepository.existsByImagePath(PATH)).thenReturn(true);

        service.deleteImage(PRODUCT_ID, SKU_ID, IMAGE_ID);

        assertThat(TransactionSynchronizationManager.getSynchronizations()).isEmpty();

        verify(imageStorage, never()).delete(any());
    }

    private void givenFirstJpegUpload() {
        OptimizedImage optimized = new OptimizedImage(new byte[]{1}, ImageFormat.JPEG);

        when(productSkuRepository.findByIdAndProductId(SKU_ID, PRODUCT_ID))
            .thenReturn(Optional.of(sku));
        when(imageOptimizer.optimize(JPEG_BYTES, ImageFormat.JPEG)).thenReturn(optimized);
        when(imageStorage.store(optimized.content(), "jpg")).thenReturn(PATH);
        when(productSkuImageRepository.existsByProductSkuId(SKU_ID)).thenReturn(false);
        when(productSkuImageRepository.existsByProductSkuProductId(PRODUCT_ID))
            .thenReturn(false);
        when(adminProductSkuImageMapper.toEntity(sku, PATH, true, true))
            .thenReturn(image);
    }

    private MockMultipartFile file(byte[] content) {
        return new MockMultipartFile(
            "file", "photo.jpg", "image/jpeg", content
        );
    }
}
