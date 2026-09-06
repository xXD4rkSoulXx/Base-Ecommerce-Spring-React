package com.d4rksoul.base.ecommerce.spring.react.service;

import com.d4rksoul.base.ecommerce.spring.react.dto.projection.ProductCoverProjection;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.ProductSearchRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.ProductShopRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.PageResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.ProductDetailsResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.ProductResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.Product;
import com.d4rksoul.base.ecommerce.spring.react.enums.OrderStatus;
import com.d4rksoul.base.ecommerce.spring.react.exception.ProductNotFoundException;
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
    public PageResponse<ProductResponse> getProducts(ProductShopRequest filter) {
        Pageable pageable = PageRequest.of(filter.page(), filter.size());

        Page<Product> productPage = switch (filter.sortBy()) {
            case "best_sellers" -> productRepository.findBestSellersPage(OrderStatus.DELIVERED, filter.category(), pageable);
            case "top_rated" -> productRepository.findTopRatedPage(3, filter.category(), pageable);
            case "discount" -> {
                Specification<Product> specification = Specification
                        .where(ProductSpecification.hasCategory(filter.category()))
                        .and(ProductSpecification.hasDiscount())
                        .and(ProductSpecification.orderByDiscountDesc());

                yield productRepository.findAll(specification, pageable);
            }
            default -> {
                Specification<Product> specification = Specification
                        .where(ProductSpecification.hasCategory(filter.category()));

                Specification<Product> orderSpecification = switch (filter.sortBy()) {
                    case "price_asc", "price_desc" -> ProductSpecification.orderByEffectivePrice(filter.sortBy());
                    default -> ProductSpecification.orderByRecentDesc();
                };

                yield productRepository.findAll(specification.and(orderSpecification), pageable);
            }
        };

        List<Long> productIds = productPage.getContent()
                .stream()
                .map(Product::getId)
                .toList();

        Map<Long, String> covers = productSkuImageRepository.findCoverByProductIds(productIds)
                .stream()
                .collect(Collectors.toMap(
                        ProductCoverProjection::productId,
                        ProductCoverProjection::path,
                        (existing, _) -> existing
                ));

        Page<ProductResponse> responsePage = productPage.map(product ->
                productMapper.toDto(product, covers.get(product.getId()))
        );

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

    @Transactional(readOnly = true)
    public ProductDetailsResponse getProductDetails(Long productId) {
        Product product = productRepository.findDetailsById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        if (product.getProductSkus() != null) {
            product.getProductSkus().forEach(sku -> {
                if (sku.getProductSkuImages() != null) {
                    org.hibernate.Hibernate.initialize(sku.getProductSkuImages());
                }
            });
        }

        List<Object[]> ratingStats = productRepository.getRatingStatsByProductId(productId);

        double averageRating = 0.0;
        long totalReviews = 0L;

        if (ratingStats != null && !ratingStats.isEmpty() && ratingStats.getFirst() != null) {
            averageRating = ratingStats.getFirst()[0] != null ? (Double) ratingStats.getFirst()[0] : 0.0;
            totalReviews = ratingStats.getFirst()[1] != null ? (Long) ratingStats.getFirst()[1] : 0L;
        }

        return productMapper.toDetailsDto(product, averageRating, totalReviews);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getRelatedProducts(Long productId) {
        Product currentProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " +  productId));

        List<Product> relatedProducts = productRepository.findByCategoryAndIdNot(
                currentProduct.getCategory(),
                productId,
                PageRequest.of(0, 4)
        );

        List<Long> productIds = relatedProducts.stream()
                .map(Product::getId)
                .toList();

        Map<Long, String> covers = productSkuImageRepository.findCoverByProductIds(productIds)
                .stream()
                .collect(Collectors.toMap(
                        ProductCoverProjection::productId,
                        ProductCoverProjection::path,
                        (existing, _) -> existing
                ));

        return relatedProducts.stream()
                .map(product -> productMapper.toDto(product, covers.get(product.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getFeaturedProducts() {
        List<Product> featuredProducts = productRepository.findByFeaturedTrue(PageRequest.of(0, 2));

        List<Long> productIds = featuredProducts.stream()
                .map(Product::getId)
                .toList();

        Map<Long, String> covers = productSkuImageRepository.findCoverByProductIds(productIds)
                .stream()
                .collect(Collectors.toMap(
                        ProductCoverProjection::productId,
                        ProductCoverProjection::path,
                        (existing, _) -> existing
                ));

        return featuredProducts.stream()
                .map(product -> productMapper.toDto(product, covers.get(product.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getDealProducts() {
        Pageable pageable = PageRequest.of(0, 8, Sort.by(Sort.Direction.DESC, "discountPercentage"));
        List<Product> dealProducts = productRepository.findByDiscountPercentageGreaterThan(0, pageable);

        List<Long> productIds = dealProducts.stream()
                .map(Product::getId)
                .toList();

        Map<Long, String> covers = productSkuImageRepository.findCoverByProductIds(productIds)
                .stream()
                .collect(Collectors.toMap(
                        ProductCoverProjection::productId,
                        ProductCoverProjection::path,
                        (existing, _) -> existing
                ));

        return dealProducts.stream()
                .map(product -> productMapper.toDto(product, covers.get(product.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getBestSellers() {
        Pageable pageable = PageRequest.of(0, 8);
        List<Product> bestSellers = productRepository.findBestSellersPage(OrderStatus.DELIVERED, null, pageable).getContent();

        List<Long> productIds = bestSellers.stream()
                .map(Product::getId)
                .toList();

        Map<Long, String> covers = productSkuImageRepository.findCoverByProductIds(productIds)
                .stream()
                .collect(Collectors.toMap(
                        ProductCoverProjection::productId,
                        ProductCoverProjection::path,
                        (existing, _) -> existing
                ));

        return bestSellers.stream()
                .map(product -> productMapper.toDto(product, covers.get(product.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getTopRatedProducts() {
        Pageable pageable = PageRequest.of(0, 8);
        List<Product> topRated = productRepository.findTopRatedPage(3, null, pageable).getContent();

        List<Long> productIds = topRated.stream()
                .map(Product::getId)
                .toList();

        Map<Long, String> covers = productSkuImageRepository.findCoverByProductIds(productIds)
                .stream()
                .collect(Collectors.toMap(
                        ProductCoverProjection::productId,
                        ProductCoverProjection::path,
                        (existing, _) -> existing
                ));

        return topRated.stream()
                .map(product -> productMapper.toDto(product, covers.get(product.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> search(ProductSearchRequest filter) {
        Specification<Product> specification = Specification
                .where(ProductSpecification.containsNameOrRef(filter.q()))
                .and(ProductSpecification.orderByRecentDesc());

        Pageable pageable = PageRequest.of(filter.page(), filter.size());
        Page<Product> productPage = productRepository.findAll(specification, pageable);

        List<Long> productIds = productPage.getContent()
                .stream()
                .map(Product::getId)
                .toList();

        Map<Long, String> covers = productSkuImageRepository.findCoverByProductIds(productIds)
                .stream()
                .collect(Collectors.toMap(
                        ProductCoverProjection::productId,
                        ProductCoverProjection::path,
                        (existing, _) -> existing
                ));

        Page<ProductResponse> responsePage = productPage.map(product ->
                productMapper.toDto(product, covers.get(product.getId()))
        );

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
