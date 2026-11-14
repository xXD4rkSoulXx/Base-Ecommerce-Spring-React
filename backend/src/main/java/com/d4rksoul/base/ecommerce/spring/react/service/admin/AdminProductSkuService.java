package com.d4rksoul.base.ecommerce.spring.react.service.admin;

import com.d4rksoul.base.ecommerce.spring.react.dto.request.admin.AdminCreateProductSkuRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.admin.AdminUpdateProductSkuRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminProductSkuResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.Product;
import com.d4rksoul.base.ecommerce.spring.react.entity.ProductSku;
import com.d4rksoul.base.ecommerce.spring.react.enums.AuditAction;
import com.d4rksoul.base.ecommerce.spring.react.exception.InvalidProductSkuException;
import com.d4rksoul.base.ecommerce.spring.react.exception.ProductNotFoundException;
import com.d4rksoul.base.ecommerce.spring.react.exception.ProductSkuNotFoundException;
import com.d4rksoul.base.ecommerce.spring.react.exception.ProductSkuAlreadyExistsException;
import com.d4rksoul.base.ecommerce.spring.react.mapper.ShippingMapper;
import com.d4rksoul.base.ecommerce.spring.react.mapper.admin.AdminProductSkuMapper;
import com.d4rksoul.base.ecommerce.spring.react.repository.ProductRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.ProductSkuRepository;
import com.d4rksoul.base.ecommerce.spring.react.service.ParcelPackingService;
import com.d4rksoul.base.ecommerce.spring.react.service.ProductSkuImageService;
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
    private final ProductSkuImageService productSkuImageService;
    private final ParcelPackingService parcelPackingService;
    private final AdminAuditLogService adminAuditLogService;
    private final AdminProductSkuMapper adminProductSkuMapper;
    private final ShippingMapper shippingMapper;

    @Transactional
    public AdminProductSkuResponse createSku(Long productId, AdminCreateProductSkuRequest request) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));

        if (!product.getCategory().allowsSize(request.size())) {
            throw new InvalidProductSkuException(
                "Size " + request.size() + " is not available for " + product.getCategory() + " products."
            );
        }

        productSkuRepository.findByProductIdAndSizeAndColor(productId, request.size(), request.color())
            .ifPresent(existing -> {
                throw new ProductSkuAlreadyExistsException(
                    existing.isActive()
                        ? "This product already has a " + request.size() + " " + request.color() + " variant."
                        : "This product has an archived " + request.size() + " " + request.color()
                            + " variant. Restore it instead."
                );
            });

        parcelPackingService.pack(List.of(shippingMapper.toPackingItem(request)));

        ProductSku sku = adminProductSkuMapper.toEntity(product, request);

        productSkuRepository.save(sku);

        adminAuditLogService.registerVariant(AuditAction.CREATED, sku, null);

        String cover = productSkuImageService.findCoversBySkuId(List.of(sku)).get(sku.getId());

        return adminProductSkuMapper.toDto(sku, cover);
    }

    @Transactional
    public void archiveSku(Long productId, Long skuId) {
        setActive(productId, skuId, false, AuditAction.ARCHIVED);
    }

    @Transactional
    public void restoreSku(Long productId, Long skuId) {
        setActive(productId, skuId, true, AuditAction.RESTORED);
    }

    @Transactional(readOnly = true)
    public List<AdminProductSkuResponse> getSkus(Long productId) {
        productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));

        List<ProductSku> skus = productSkuRepository.findByProductIdOrderByIdAsc(productId);
        Map<Long, String> covers = productSkuImageService.findCoversBySkuId(skus);

        return skus.stream()
            .map(sku ->
                adminProductSkuMapper.toDto(sku, covers.get(sku.getId()))
            )
            .toList();
    }

    @Transactional
    public AdminProductSkuResponse updateSku(Long productId, Long skuId, AdminUpdateProductSkuRequest request) {
        ProductSku sku = productSkuRepository.findByIdAndProductId(skuId, productId)
            .orElseThrow(() -> new ProductSkuNotFoundException("Product SKU not found with id: " + skuId));

        parcelPackingService.pack(List.of(shippingMapper.toPackingItem(request)));

        sku.setWeightGrams(request.weightGrams());
        sku.setLengthCm(request.lengthCm());
        sku.setWidthCm(request.widthCm());
        sku.setHeightCm(request.heightCm());
        sku.setDiscountPercentage(request.discountPercentage());

        adminAuditLogService.registerVariant(AuditAction.UPDATED, sku, null);

        String cover = productSkuImageService.findCoversBySkuId(List.of(sku)).get(sku.getId());

        return adminProductSkuMapper.toDto(sku, cover);
    }

    private void setActive(Long productId, Long skuId, boolean active, AuditAction action) {
        ProductSku sku = productSkuRepository.findByIdAndProductId(skuId, productId)
            .orElseThrow(() -> new ProductSkuNotFoundException("Product SKU not found with id: " + skuId));

        sku.setActive(active);

        adminAuditLogService.registerVariant(action, sku, null);
    }
}
