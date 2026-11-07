package com.d4rksoul.base.ecommerce.spring.react.service.admin;

import com.d4rksoul.base.ecommerce.spring.react.dto.request.admin.AdminStockSearchRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.PageResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminStockItemResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.ProductSku;
import com.d4rksoul.base.ecommerce.spring.react.exception.InsufficientStockException;
import com.d4rksoul.base.ecommerce.spring.react.exception.ProductSkuNotFoundException;
import com.d4rksoul.base.ecommerce.spring.react.mapper.PageMapper;
import com.d4rksoul.base.ecommerce.spring.react.mapper.admin.AdminStockMapper;
import com.d4rksoul.base.ecommerce.spring.react.repository.ProductSkuRepository;
import com.d4rksoul.base.ecommerce.spring.react.service.ProductImageService;
import com.d4rksoul.base.ecommerce.spring.react.specification.ProductSkuSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminStockService {

    private final ProductSkuRepository productSkuRepository;
    private final ProductImageService productImageService;
    private final AdminStockMapper adminStockMapper;
    private final PageMapper pageMapper;

    @Transactional
    public void adjustStock(Long skuId, int delta) {
        if (productSkuRepository.adjustStock(skuId, delta) == 0) {
            throw productSkuRepository.existsById(skuId)
                    ? new InsufficientStockException("Stock cannot go below zero.")
                    : new ProductSkuNotFoundException("Product SKU not found with id: " + skuId);
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminStockItemResponse> getStock(AdminStockSearchRequest filter) {
        Specification<ProductSku> specification = Specification
                .where(ProductSkuSpecification.isActive())
                .and(ProductSkuSpecification.containsProductNameOrRef(filter.q()))
                .and(ProductSkuSpecification.hasLowStock(filter.lowStock(), 5))
                .and(ProductSkuSpecification.orderByProductName());

        Pageable pageable = PageRequest.of(filter.page(), filter.size());
        Page<ProductSku> skusPage = productSkuRepository.findAll(specification, pageable);

        Map<Long, String> covers = productImageService.findCoversBySkuId(skusPage.getContent());

        Page<AdminStockItemResponse> responsePage = skusPage
                .map(sku -> adminStockMapper.toDto(sku, covers.get(sku.getId())));

        return pageMapper.toDto(responsePage);
    }

    @Transactional
    public void setStock(Long skuId, int stock) {
        if (productSkuRepository.setStock(skuId, stock) == 0) {
            throw new ProductSkuNotFoundException("Product SKU not found with id: " + skuId);
        }
    }
}
