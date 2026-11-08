package com.d4rksoul.base.ecommerce.spring.react.service.admin;

import com.d4rksoul.base.ecommerce.spring.react.dto.request.admin.AdminCreateProductSkuRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.admin.AdminUpdateProductSkuRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminProductSkuResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.result.PackingItemResult;
import com.d4rksoul.base.ecommerce.spring.react.entity.Product;
import com.d4rksoul.base.ecommerce.spring.react.entity.ProductSku;
import com.d4rksoul.base.ecommerce.spring.react.enums.AuditAction;
import com.d4rksoul.base.ecommerce.spring.react.exception.ProductNotFoundException;
import com.d4rksoul.base.ecommerce.spring.react.exception.ProductSkuNotFoundException;
import com.d4rksoul.base.ecommerce.spring.react.exception.ProductSkuAlreadyExistsException;
import com.d4rksoul.base.ecommerce.spring.react.mapper.admin.AdminProductSkuMapper;
import com.d4rksoul.base.ecommerce.spring.react.repository.ProductRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.ProductSkuRepository;
import com.d4rksoul.base.ecommerce.spring.react.service.ParcelPackingService;
import com.d4rksoul.base.ecommerce.spring.react.service.ProductImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminProductSkuService {

    private final ProductRepository productRepository;
    private final ProductSkuRepository productSkuRepository;
    private final ProductImageService productImageService;
    private final ParcelPackingService parcelPackingService;
    private final AdminAuditLogService adminAuditLogService;
    private final AdminProductSkuMapper adminProductSkuMapper;

    @Transactional
    public AdminProductSkuResponse createSku(Long productId, AdminCreateProductSkuRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));

        productSkuRepository.findByProductIdAndSizeAndColor(productId, request.size(), request.color())
                .ifPresent(existing -> {
                    throw new ProductSkuAlreadyExistsException(
                            existing.isActive()
                                    ? "This product already has a "
                                        + request.size()
                                        + " "
                                        + request.color()
                                        + " variant."
                                    : "This product has an archived "
                                        + request.size()
                                        + " "
                                        + request.color()
                                        + " variant. Restore it instead."
                    );
                });

        parcelPackingService.pack(List.of(
                new PackingItemResult(
                        null,
                        request.weight() / 1000.0,
                        request.length(),
                        request.width(),
                        request.height()
                )
        ));

        ProductSku sku = productSkuRepository.save(adminProductSkuMapper.toEntity(product, request));

        adminAuditLogService.registerVariant(AuditAction.CREATED, sku, null);

        String cover = productImageService.findCoversBySkuId(List.of(sku)).get(sku.getId());

        return adminProductSkuMapper.toDto(sku, cover);
    }

    @Transactional
    public void archiveSku(Long productId, Long skuId) {
        ProductSku sku = productSkuRepository.findByIdAndProductId(skuId, productId)
                .orElseThrow(() -> new ProductSkuNotFoundException("Product SKU not found with id: " + skuId));

        sku.setActive(false);

        adminAuditLogService.registerVariant(AuditAction.ARCHIVED, sku, null);
    }

    @Transactional
    public void restoreSku(Long productId, Long skuId) {
        ProductSku sku = productSkuRepository.findByIdAndProductId(skuId, productId)
                .orElseThrow(() -> new ProductSkuNotFoundException("Product SKU not found with id: " + skuId));

        sku.setActive(true);

        adminAuditLogService.registerVariant(AuditAction.RESTORED, sku, null);
    }

    @Transactional(readOnly = true)
    public List<AdminProductSkuResponse> getSkus(Long productId) {
        productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));

        List<ProductSku> skus = productSkuRepository.findByProductIdOrderByIdAsc(productId);
        Map<Long, String> covers = productImageService.findCoversBySkuId(skus);

        return skus.stream()
                .map(sku -> adminProductSkuMapper.toDto(sku, covers.get(sku.getId())))
                .toList();
    }

    @Transactional
    public AdminProductSkuResponse updateSku(Long productId, Long skuId, AdminUpdateProductSkuRequest request) {
        ProductSku sku = productSkuRepository.findByIdAndProductId(skuId, productId)
                .orElseThrow(() -> new ProductSkuNotFoundException("Product SKU not found with id: " + skuId));

        parcelPackingService.pack(List.of(
                new PackingItemResult(
                        null,
                        request.weight() / 1000.0,
                        request.length(),
                        request.width(),
                        request.height()
                )
        ));

        adminProductSkuMapper.updateEntity(sku, request);

        adminAuditLogService.registerVariant(AuditAction.UPDATED, sku, null);

        String cover = productImageService.findCoversBySkuId(List.of(sku)).get(sku.getId());

        return adminProductSkuMapper.toDto(sku, cover);
    }
}
