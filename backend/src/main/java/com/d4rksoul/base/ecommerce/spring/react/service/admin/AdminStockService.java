package com.d4rksoul.base.ecommerce.spring.react.service.admin;

import com.d4rksoul.base.ecommerce.spring.react.constant.Constants;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.admin.AdminStockSearchRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.PageResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminStockItemResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.ProductSku;
import com.d4rksoul.base.ecommerce.spring.react.enums.AuditAction;
import com.d4rksoul.base.ecommerce.spring.react.exception.InsufficientStockException;
import com.d4rksoul.base.ecommerce.spring.react.exception.InvalidStockAdjustmentException;
import com.d4rksoul.base.ecommerce.spring.react.exception.ProductSkuNotFoundException;
import com.d4rksoul.base.ecommerce.spring.react.mapper.PageMapper;
import com.d4rksoul.base.ecommerce.spring.react.mapper.admin.AdminStockMapper;
import com.d4rksoul.base.ecommerce.spring.react.repository.ProductSkuRepository;
import com.d4rksoul.base.ecommerce.spring.react.service.ProductSkuImageService;
import com.d4rksoul.base.ecommerce.spring.react.specification.ProductSkuSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminStockService {

    private final ProductSkuRepository productSkuRepository;
    private final ProductSkuImageService productSkuImageService;
    private final AdminAuditLogService adminAuditLogService;
    private final AdminStockMapper adminStockMapper;
    private final PageMapper pageMapper;

    @Transactional
    public void adjustStock(Long skuId, int delta) {
        if (delta == 0) {
            throw new InvalidStockAdjustmentException("Adjustment cannot be zero.");
        }

        int updatedRows = productSkuRepository.adjustStock(skuId, delta);
        if (updatedRows == 0) {
            boolean skuExists = productSkuRepository.existsById(skuId);
            if (skuExists) {
                throw new InsufficientStockException("Stock cannot go below zero.");
            }

            throw new ProductSkuNotFoundException("Product SKU not found with id: " + skuId);
        }

        ProductSku sku = productSkuRepository.findById(skuId)
            .orElseThrow(() -> new ProductSkuNotFoundException("Product SKU not found with id: " + skuId));

        adminAuditLogService.registerVariant(
            AuditAction.STOCK_ADJUSTED,
            sku,
            String.format(Locale.ROOT, "%+d (stock now %d)", delta, sku.getStock())
        );
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminStockItemResponse> getStock(AdminStockSearchRequest filter) {
        Specification<ProductSku> specification = Specification
            .where(ProductSkuSpecification.isActive())
            .and(ProductSkuSpecification.containsProductNameOrRef(filter.q()))
            .and(ProductSkuSpecification.hasLowStock(filter.lowStock(), Constants.LOW_STOCK_THRESHOLD))
            .and(ProductSkuSpecification.orderByProductName());

        Pageable pageable = PageRequest.of(filter.page(), filter.size());
        Page<ProductSku> skusPage = productSkuRepository.findAll(specification, pageable);

        Map<Long, String> covers = productSkuImageService.findCoversBySkuId(skusPage.getContent());

        Page<AdminStockItemResponse> responsePage = skusPage
            .map(sku -> adminStockMapper.toDto(sku, covers.get(sku.getId())));

        return pageMapper.toDto(responsePage);
    }

    @Transactional
    public void setStock(Long skuId, int stock) {
        int updatedRows = productSkuRepository.setStock(skuId, stock);
        if (updatedRows == 0) {
            throw new ProductSkuNotFoundException("Product SKU not found with id: " + skuId);
        }

        ProductSku sku = productSkuRepository.findById(skuId)
            .orElseThrow(() -> new ProductSkuNotFoundException("Product SKU not found with id: " + skuId));

        adminAuditLogService.registerVariant(AuditAction.STOCK_SET, sku, "Set to " + stock);
    }
}
