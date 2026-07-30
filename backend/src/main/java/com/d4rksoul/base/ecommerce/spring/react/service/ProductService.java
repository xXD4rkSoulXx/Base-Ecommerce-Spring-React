package com.d4rksoul.base.ecommerce.spring.react.service;

import com.d4rksoul.base.ecommerce.spring.react.dto.request.ProductShopRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.ProductResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.Product;
import com.d4rksoul.base.ecommerce.spring.react.entity.ProductSkuImage;
import com.d4rksoul.base.ecommerce.spring.react.mapper.ProductMapper;
import com.d4rksoul.base.ecommerce.spring.react.repository.ProductRepository;
import com.d4rksoul.base.ecommerce.spring.react.specification.ProductSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Transactional(readOnly = true)
    public Page<ProductResponse> findProducts(ProductShopRequest filter) {
        Specification<Product> specification = Specification
                .where(ProductSpecification.hasCategory(filter.category()));

        Sort sort = switch (filter.sortBy()) {
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "price");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "price");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };

        Pageable pageable = PageRequest.of(filter.page(), filter.size(), sort);

        Page<Product> productPage = productRepository.findAll(specification, pageable);

        return productPage.map(product -> {
            String coverPath = product.getProductSkus().stream()
                    .flatMap(sku -> sku.getProductSkuImages().stream())
                    .filter((ProductSkuImage::isCover))
                    .map(ProductSkuImage::getPath)
                    .findFirst()
                    .orElse(null);

            return productMapper.toDto(product, coverPath);
        });
    }
}
