package com.d4rksoul.base.ecommerce.spring.react.service;

import com.d4rksoul.base.ecommerce.spring.react.dto.projection.ProductCoverProjection;
import com.d4rksoul.base.ecommerce.spring.react.dto.projection.ProductSkuCoverProjection;
import com.d4rksoul.base.ecommerce.spring.react.entity.ProductSku;
import com.d4rksoul.base.ecommerce.spring.react.repository.ProductSkuImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductImageService {

    private final ProductSkuImageRepository productSkuImageRepository;

    public Map<Long, String> findCoversBySkuId(List<ProductSku> skus) {
        List<Long> skuIds = skus.stream()
                .map(ProductSku::getId)
                .distinct()
                .toList();

        List<Long> productIds = skus.stream()
                .map(sku -> sku.getProduct().getId())
                .distinct()
                .toList();

        Map<Long, String> skuCovers = productSkuImageRepository.findImagesByProductSkuIds(skuIds)
                .stream()
                .collect(Collectors.toMap(
                        ProductSkuCoverProjection::productSkuId,
                        ProductSkuCoverProjection::path,
                        (existing, _) -> existing
                ));

        Map<Long, String> productCovers = productSkuImageRepository.findCoverByProductIds(productIds)
                .stream()
                .collect(Collectors.toMap(
                        ProductCoverProjection::productId,
                        ProductCoverProjection::path,
                        (existing, _) -> existing
                ));

        Map<Long, String> covers = new HashMap<>();

        for (ProductSku sku : skus) {
            String cover = skuCovers.getOrDefault(sku.getId(), productCovers.get(sku.getProduct().getId()));

            if (cover != null) {
                covers.put(sku.getId(), cover);
            }
        }

        return covers;
    }

    public Map<Long, String> findCoversByProductId(List<Long> productIds) {
        return productSkuImageRepository.findCoverByProductIds(productIds)
                .stream()
                .collect(Collectors.toMap(
                        ProductCoverProjection::productId,
                        ProductCoverProjection::path,
                        (existing, _) -> existing
                ));
    }
}
