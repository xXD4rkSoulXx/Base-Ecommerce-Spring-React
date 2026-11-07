package com.d4rksoul.base.ecommerce.spring.react.service.admin;

import com.d4rksoul.base.ecommerce.spring.react.dto.projection.ProductStockProjection;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.admin.AdminProductRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.admin.AdminProductSearchRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.PageResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminProductDetailsResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminProductSummaryResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.Product;
import com.d4rksoul.base.ecommerce.spring.react.enums.FeaturedSlot;
import com.d4rksoul.base.ecommerce.spring.react.exception.ProductNotFoundException;
import com.d4rksoul.base.ecommerce.spring.react.exception.FeaturedSlotTakenException;
import com.d4rksoul.base.ecommerce.spring.react.exception.ProductRefAlreadyExistsException;
import com.d4rksoul.base.ecommerce.spring.react.mapper.PageMapper;
import com.d4rksoul.base.ecommerce.spring.react.mapper.admin.AdminProductMapper;
import com.d4rksoul.base.ecommerce.spring.react.repository.ProductRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.ProductSkuRepository;
import com.d4rksoul.base.ecommerce.spring.react.service.ProductImageService;
import com.d4rksoul.base.ecommerce.spring.react.specification.ProductSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminProductService {

    private final ProductRepository productRepository;
    private final ProductSkuRepository productSkuRepository;
    private final ProductImageService productImageService;
    private final AdminProductMapper adminProductMapper;
    private final PageMapper pageMapper;

    @Transactional
    public AdminProductDetailsResponse createProduct(AdminProductRequest request) {
        if (productRepository.existsByRef(request.ref())) {
            throw new ProductRefAlreadyExistsException("A product with ref " + request.ref() + " already exists.");
        }

        validateFeaturedSlot(request.featuredSlot(), null);

        Product product = productRepository.save(adminProductMapper.toEntity(request));

        return adminProductMapper.toDetailsDto(product);
    }

    @Transactional
    public void archiveProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));

        product.setActive(false);
        product.setFeaturedSlot(null);
    }

    @Transactional
    public void restoreProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));

        product.setActive(true);
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminProductSummaryResponse> getProducts(AdminProductSearchRequest filter) {
        Specification<Product> specification = Specification
                .where(ProductSpecification.containsNameOrRef(filter.q()))
                .and(ProductSpecification.hasCategory(filter.category()))
                .and(ProductSpecification.hasLowStock(filter.lowStock(), 5))
                .and(ProductSpecification.orderByRecentDesc());

        Pageable pageable = PageRequest.of(filter.page(), filter.size());
        Page<Product> productsPage = productRepository.findAll(specification, pageable);

        List<Long> productIds = productsPage.getContent().stream()
                .map(Product::getId)
                .toList();

        Map<Long, String> covers = productImageService.findCoversByProductId(productIds);
        Map<Long, ProductStockProjection> stocks = productSkuRepository
                .summarizeStockByProductIds(productIds, 5)
                .stream()
                .collect(Collectors.toMap(ProductStockProjection::productId, Function.identity()));

        Page<AdminProductSummaryResponse> responsePage = productsPage.map(product ->
                adminProductMapper.toSummaryDto(
                        product,
                        covers.get(product.getId()),
                        stocks.getOrDefault(
                                product.getId(),
                                new ProductStockProjection(
                                        product.getId(),
                                        0L,
                                        0L,
                                        0L
                                )
                        )
                )
        );

        return pageMapper.toDto(responsePage);
    }

    @Transactional(readOnly = true)
    public AdminProductDetailsResponse getProduct(Long id) {
        return adminProductMapper.toDetailsDto(
                productRepository.findById(id)
                        .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id))
        );
    }

    @Transactional
    public AdminProductDetailsResponse updateProduct(Long id, AdminProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));

        if (productRepository.existsByRefAndIdNot(request.ref(), id)) {
            throw new ProductRefAlreadyExistsException("A product with ref " + request.ref() + " already exists.");
        }

        validateFeaturedSlot(request.featuredSlot(), id);
        adminProductMapper.updateEntity(product, request);

        return adminProductMapper.toDetailsDto(product);
    }

    private void validateFeaturedSlot(FeaturedSlot slot, Long productId) {
        if (slot == null) {
            return;
        }

        productRepository.findByFeaturedSlot(slot)
                .filter(holder -> !holder.getId().equals(productId))
                .ifPresent(holder -> {
                    throw new FeaturedSlotTakenException(
                            "The " + slot.name().toLowerCase() + " slot is taken by " + holder.getName()
                                    + ". Remove it first."
                    );
                });
    }
}
