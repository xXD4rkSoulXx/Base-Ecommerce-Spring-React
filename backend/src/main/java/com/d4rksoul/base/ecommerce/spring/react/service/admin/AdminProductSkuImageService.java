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
import com.d4rksoul.base.ecommerce.spring.react.storage.ImageStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminProductSkuImageService {

    private static final byte[] JPEG_SIGNATURE = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PNG_SIGNATURE = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'};
    private static final byte[] RIFF = "RIFF".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] WEBP = "WEBP".getBytes(StandardCharsets.US_ASCII);

    private final ProductSkuRepository productSkuRepository;
    private final ProductSkuImageRepository productSkuImageRepository;
    private final OrderItemRepository orderItemRepository;
    private final ImageStorage imageStorage;
    private final AdminAuditLogService adminAuditLogService;
    private final AdminProductSkuImageMapper adminProductSkuImageMapper;

    @Transactional
    public AdminProductSkuImageResponse uploadImage(Long productId, Long skuId, MultipartFile file) {
        ProductSku sku = productSkuRepository.findByIdAndProductId(skuId, productId)
            .orElseThrow(() -> new ProductSkuNotFoundException("Product SKU not found with id: " + skuId));

        if (file.isEmpty()) {
            throw new InvalidImageException("Select an image to upload.");
        }

        byte[] content;
        try {
            content = file.getBytes();
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }

        String path = imageStorage.store(content, detectFormat(content).getExtension());

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) {
                    imageStorage.delete(path);
                }
            }
        });

        boolean skuCover = !productSkuImageRepository.existsByProductSkuId(skuId);
        boolean productCover = !productSkuImageRepository.existsByProductSkuProductId(productId);

        ProductSkuImage image = adminProductSkuImageMapper.toEntity(sku, path, skuCover, productCover);

        productSkuImageRepository.save(image);

        adminAuditLogService.registerVariant(AuditAction.IMAGE_UPLOADED, sku, null);

        return adminProductSkuImageMapper.toDto(image);
    }

    @Transactional(readOnly = true)
    public List<AdminProductSkuImageResponse> getImages(Long productId, Long skuId) {
        productSkuRepository.findByIdAndProductId(skuId, productId)
            .orElseThrow(() -> new ProductSkuNotFoundException("Product SKU not found with id: " + skuId));

        return productSkuImageRepository.findByProductSkuIdOrderByIdAsc(skuId)
            .stream()
            .map(adminProductSkuImageMapper::toDto)
            .toList();
    }

    @Transactional
    public void setProductCover(Long productId, Long skuId, Long imageId) {
        changeCover(productId, skuId, imageId, "Product cover", () -> {
            productSkuImageRepository.clearProductCover(productId);
            productSkuImageRepository.markProductCover(imageId);
        });
    }

    @Transactional
    public void setSkuCover(Long productId, Long skuId, Long imageId) {
        changeCover(productId, skuId, imageId, "Variant cover", () -> {
            productSkuImageRepository.clearSkuCover(skuId);
            productSkuImageRepository.markSkuCover(imageId);
        });
    }

    @Transactional
    public void deleteImage(Long productId, Long skuId, Long imageId) {
        ProductSku sku = productSkuRepository.findByIdAndProductId(skuId, productId)
            .orElseThrow(() -> new ProductSkuNotFoundException("Product SKU not found with id: " + skuId));

        ProductSkuImage image = productSkuImageRepository.findByIdAndProductSkuId(imageId, skuId)
            .orElseThrow(() -> new ProductSkuImageNotFoundException("Image not found with id: " + imageId));

        productSkuImageRepository.delete(image);
        productSkuImageRepository.flush();

        if (image.isSkuCover()) {
            productSkuImageRepository.findFirstByProductSkuIdOrderByIdAsc(skuId)
                .ifPresent(next -> productSkuImageRepository.markSkuCover(next.getId()));
        }

        if (image.isProductCover()) {
            productSkuImageRepository.findFirstByProductSkuProductIdOrderByIdAsc(productId)
                .ifPresent(next -> productSkuImageRepository.markProductCover(next.getId()));
        }

        adminAuditLogService.registerVariant(AuditAction.IMAGE_DELETED, sku, null);

        boolean usedByOrders = orderItemRepository.existsByImagePath(image.getPath());
        if (!usedByOrders) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    imageStorage.delete(image.getPath());
                }
            });
        }
    }

    private void changeCover(
        Long productId, Long skuId, Long imageId, String coverType, Runnable update
    ) {
        ProductSku sku = productSkuRepository.findByIdAndProductId(skuId, productId)
            .orElseThrow(() -> new ProductSkuNotFoundException("Product SKU not found with id: " + skuId));

        productSkuImageRepository.findByIdAndProductSkuId(imageId, skuId)
            .orElseThrow(() -> new ProductSkuImageNotFoundException("Image not found with id: " + imageId));

        update.run();

        adminAuditLogService.registerVariant(AuditAction.COVER_CHANGED, sku, coverType);
    }

    private ImageFormat detectFormat(byte[] content) {
        if (startsWith(content, JPEG_SIGNATURE)) {
            return ImageFormat.JPEG;
        }

        if (startsWith(content, PNG_SIGNATURE)) {
            return ImageFormat.PNG;
        }

        boolean isWebp = content.length >= 12
            && Arrays.equals(content, 0, 4, RIFF, 0, 4)
            && Arrays.equals(content, 8, 12, WEBP, 0, 4);
        if (isWebp) {
            return ImageFormat.WEBP;
        }

        throw new InvalidImageException("Only JPEG, PNG and WebP images are accepted.");
    }

    private boolean startsWith(byte[] content, byte[] signature) {
        return content.length >= signature.length
            && Arrays.equals(
                content, 0, signature.length, signature, 0, signature.length
            );
    }
}
