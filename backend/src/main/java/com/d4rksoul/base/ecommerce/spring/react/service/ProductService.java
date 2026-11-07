package com.d4rksoul.base.ecommerce.spring.react.service;

import com.d4rksoul.base.ecommerce.spring.react.dto.projection.ProductCoverProjection;
import com.d4rksoul.base.ecommerce.spring.react.dto.projection.RatingStatsProjection;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.ProductSearchRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.ProductShopRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.PageResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.ProductDetailsResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.ProductResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.Product;
import com.d4rksoul.base.ecommerce.spring.react.enums.OrderStatus;
import com.d4rksoul.base.ecommerce.spring.react.exception.ProductNotFoundException;
import com.d4rksoul.base.ecommerce.spring.react.mapper.PageMapper;
import com.d4rksoul.base.ecommerce.spring.react.mapper.ProductMapper;
import com.d4rksoul.base.ecommerce.spring.react.repository.ProductRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.ProductSkuImageRepository;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductSkuImageRepository productSkuImageRepository;
    private final ProductMapper productMapper;
    private final PageMapper pageMapper;

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getProducts(ProductShopRequest filter) {
        Pageable pageable = PageRequest.of(filter.page(), filter.size());

        Page<Product> productPage = switch (filter.sortBy()) {
            case "best_sellers" -> productRepository.findBestSellersPage(
                    OrderStatus.DELIVERED,
                    filter.category(),
                    pageable
            );
            case "top_rated" -> productRepository.findTopRatedPage(3, filter.category(), pageable);
            case "discount" -> {
                Specification<Product> specification = Specification
                        .where(ProductSpecification.isVisibleInShop())
                        .and(ProductSpecification.hasCategory(filter.category()))
                        .and(ProductSpecification.hasDiscount())
                        .and(ProductSpecification.orderByDiscountDesc());

                yield productRepository.findAll(specification, pageable);
            }
            default -> {
                Specification<Product> specification = Specification
                        .where(ProductSpecification.isVisibleInShop())
                        .and(ProductSpecification.hasCategory(filter.category()));

                Specification<Product> orderSpecification = switch (filter.sortBy()) {
                    case "price_asc", "price_desc" -> ProductSpecification.orderByEffectivePrice(filter.sortBy());
                    default -> ProductSpecification.orderByRecentDesc();
                };

                yield productRepository.findAll(specification.and(orderSpecification), pageable);
            }
        };

        return mapProductPageToDto(productPage);
    }

    @Transactional(readOnly = true)
    public ProductDetailsResponse getProductDetails(Long productId) {
        Product product = productRepository.findVisibleDetailsById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));

        if (product.getProductSkus() != null) {
            product.getProductSkus().forEach(sku -> {
                if (sku.getProductSkuImages() != null) {
                    org.hibernate.Hibernate.initialize(sku.getProductSkuImages());
                }
            });
        }

        RatingStatsProjection stats = productRepository.findRatingStatsByProductId(productId)
                .orElse(new RatingStatsProjection(0.0, 0L));

        double averageRating = stats.averageRating();
        long totalReviews = stats.totalReviews();

        return productMapper.toDetailsDto(product, averageRating, totalReviews);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getRelatedProducts(Long productId) {
        Product currentProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " +  productId));

        Specification<Product> specification = Specification
                .where(ProductSpecification.isVisibleInShop())
                .and(ProductSpecification.hasCategory(currentProduct.getCategory()))
                .and(ProductSpecification.hasIdNot(productId))
                .and(ProductSpecification.orderByRecentDesc());

        List<Product> relatedProducts = productRepository.findAll(
                    specification,
                    PageRequest.of(0, 4)
                ).getContent();

        return mapProductsToDto(relatedProducts);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getFeaturedProducts() {
        Specification<Product> specification = Specification
                .where(ProductSpecification.isVisibleInShop())
                .and(ProductSpecification.hasFeaturedSlot());

        List<Product> featuredProducts = productRepository.findAll(specification);

        return mapProductsToDto(featuredProducts);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getDealProducts() {
        Specification<Product> specification = Specification
                .where(ProductSpecification.isVisibleInShop())
                .and(ProductSpecification.hasDiscount())
                .and(ProductSpecification.orderByDiscountDesc());

        List<Product> dealProducts = productRepository.findAll(
                        specification,
                        PageRequest.of(0, 8)
                ).getContent();

        return mapProductsToDto(dealProducts);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getBestSellers() {
        Pageable pageable = PageRequest.of(0, 8);
        List<Product> bestSellers = productRepository.findBestSellersPage(
                OrderStatus.DELIVERED,
                null,
                pageable
        ).getContent();

        return mapProductsToDto(bestSellers);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getTopRatedProducts() {
        Pageable pageable = PageRequest.of(0, 8);
        List<Product> topRated = productRepository.findTopRatedPage(
                3,
                null,
                pageable
        ).getContent();

        return mapProductsToDto(topRated);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> search(ProductSearchRequest filter) {
        Specification<Product> specification = Specification
                .where(ProductSpecification.isVisibleInShop())
                .and(ProductSpecification.containsNameOrRef(filter.q()))
                .and(ProductSpecification.orderByRecentDesc());

        Pageable pageable = PageRequest.of(filter.page(), filter.size());
        Page<Product> productPage = productRepository.findAll(specification, pageable);

        return mapProductPageToDto(productPage);
    }

    private List<ProductResponse> mapProductsToDto(List<Product> products) {
        List<Long> productIds = products.stream()
                .map(Product::getId)
                .toList();

        Map<Long, String> covers = productSkuImageRepository.findCoverByProductIds(productIds)
                .stream()
                .collect(Collectors.toMap(
                        ProductCoverProjection::productId,
                        ProductCoverProjection::path,
                        (existing, _) -> existing
                ));

        return products.stream()
                .map(product -> productMapper.toDto(product, covers.get(product.getId())))
                .toList();
    }

    private PageResponse<ProductResponse> mapProductPageToDto(Page<Product> productPage) {
        List<Long> productIds = productPage.getContent().stream()
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

        return pageMapper.toDto(responsePage);
    }
}
