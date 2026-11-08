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

        ImageFormat format = detectFormat(content);
        String url = imageStorage.store(content, format.getExtension());
        boolean cover = !productSkuImageRepository.existsByProductSkuId(skuId);

        ProductSkuImage image = productSkuImageRepository.save(
                adminProductSkuImageMapper.toEntity(sku, url, cover)
        );

        adminAuditLogService.registerVariant(AuditAction.IMAGE_UPLOADED, sku, null);

        return adminProductSkuImageMapper.toDto(image);
    }

    @Transactional(readOnly = true)
    public List<AdminProductSkuImageResponse> getImages(Long productId, Long skuId) {
        productSkuRepository.findByIdAndProductId(skuId, productId)
                .orElseThrow(() -> new ProductSkuNotFoundException("Product SKU not found with id: " + skuId));

        return productSkuImageRepository.findByProductSkuIdOrderByIdAsc(skuId).stream()
                .map(adminProductSkuImageMapper::toDto)
                .toList();
    }

    @Transactional
    public void setCover(Long productId, Long skuId, Long imageId) {
        ProductSku sku = productSkuRepository.findByIdAndProductId(skuId, productId)
                .orElseThrow(() -> new ProductSkuNotFoundException("Product SKU not found with id: " + skuId));

        List<ProductSkuImage> images = productSkuImageRepository.findByProductSkuIdOrderByIdAsc(skuId);

        if (images.stream().noneMatch(image -> image.getId().equals(imageId))) {
            throw new ProductSkuImageNotFoundException("Image not found with id: " + imageId);
        }

        images.forEach(image -> image.setCover(image.getId().equals(imageId)));

        adminAuditLogService.registerVariant(AuditAction.COVER_CHANGED, sku, null);
    }

    @Transactional
    public void deleteImage(Long productId, Long skuId, Long imageId) {
        ProductSku sku = productSkuRepository.findByIdAndProductId(skuId, productId)
                .orElseThrow(() -> new ProductSkuNotFoundException("Product SKU not found with id: " + skuId));

        ProductSkuImage image = productSkuImageRepository.findByIdAndProductSkuId(imageId, skuId)
                .orElseThrow(() -> new ProductSkuImageNotFoundException("Image not found with id: " + imageId));

        if (image.isCover()) {
            productSkuImageRepository.findByProductSkuIdOrderByIdAsc(skuId).stream()
                    .filter(other -> !other.getId().equals(imageId))
                    .findFirst()
                    .ifPresent(next -> next.setCover(true));
        }

        productSkuImageRepository.delete(image);

        adminAuditLogService.registerVariant(AuditAction.IMAGE_DELETED, sku, null);

        if (!orderItemRepository.existsByImagePath(image.getPath())) {
            imageStorage.delete(image.getPath());
        }
    }

    private ImageFormat detectFormat(byte[] content) {
        if (startsWith(content, JPEG_SIGNATURE)) {
            return ImageFormat.JPEG;
        }

        if (startsWith(content, PNG_SIGNATURE)) {
            return ImageFormat.PNG;
        }

        if (content.length >= 12
                && Arrays.equals(content, 0, 4, RIFF, 0, 4)
                && Arrays.equals(content, 8, 12, WEBP, 0, 4)
        ) {
            return ImageFormat.WEBP;
        }

        throw new InvalidImageException("Only JPEG, PNG and WebP images are accepted.");
    }

    private boolean startsWith(byte[] content, byte[] signature) {
        return content.length >= signature.length
                && Arrays.equals(content, 0, signature.length, signature, 0, signature.length);
    }
}
