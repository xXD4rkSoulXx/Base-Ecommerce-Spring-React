package com.d4rksoul.base.ecommerce.spring.react.service;

import com.d4rksoul.base.ecommerce.spring.react.dto.internal.ProductCoverResult;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.ProductShopRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.PageResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.ProductResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.Product;
import com.d4rksoul.base.ecommerce.spring.react.mapper.ProductMapper;
import com.d4rksoul.base.ecommerce.spring.react.repository.ProductRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.ProductSkuImageRepository;
import com.d4rksoul.base.ecommerce.spring.react.specification.ProductSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductSkuImageRepository productSkuImageRepository;
    private final ProductMapper productMapper;

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> findProducts(ProductShopRequest filter) {
        Specification<Product> specification = Specification
                .where(ProductSpecification.hasCategory(filter.category()));

        Sort sort = switch (filter.sortBy()) {
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "price");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "price");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };

        Pageable pageable = PageRequest.of(filter.page(), filter.size(), sort);

        Page<Product> productPage = productRepository.findAll(specification, pageable);

        List<Long> productIds = productPage.getContent()
                .stream()
                .map(Product::getId)
                .toList();

        Map<Long, String> covers = productSkuImageRepository.findCoverPaths(productIds)
                .stream()
                .collect(Collectors.toMap(ProductCoverResult::productId, ProductCoverResult::path));

        Page<ProductResponse> responsePage = productPage.map(product -> productMapper.toDto(product, covers.get(product.getId())));

        return PageResponse.<ProductResponse>builder()
                .content(responsePage.getContent())
                .page(responsePage.getNumber())
                .size(responsePage.getSize())
                .totalElements(responsePage.getTotalElements())
                .totalPages(responsePage.getTotalPages())
                .first(responsePage.isFirst())
                .last(responsePage.isLast())
                .build();
    }
}
