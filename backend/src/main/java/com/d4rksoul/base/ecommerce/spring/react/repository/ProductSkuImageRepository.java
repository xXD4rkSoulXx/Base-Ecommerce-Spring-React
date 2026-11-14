package com.d4rksoul.base.ecommerce.spring.react.repository;

import com.d4rksoul.base.ecommerce.spring.react.dto.projection.ProductCoverProjection;
import com.d4rksoul.base.ecommerce.spring.react.dto.projection.ProductSkuCoverProjection;
import com.d4rksoul.base.ecommerce.spring.react.entity.ProductSkuImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductSkuImageRepository extends JpaRepository<ProductSkuImage, Long> {

    @Query("""
        SELECT new com.d4rksoul.base.ecommerce.spring.react.dto.projection.ProductCoverProjection(
            sku.product.id,
            psi.path
        )
        FROM ProductSkuImage psi
        JOIN psi.productSku sku
        WHERE psi.productCover = true
        AND sku.product.id IN :productIds
    """)
    List<ProductCoverProjection> findCoverByProductIds(@Param("productIds") List<Long> productIds);

    @Query("""
        SELECT new com.d4rksoul.base.ecommerce.spring.react.dto.projection.ProductSkuCoverProjection(
            sku.id,
            psi.path
        )
        FROM ProductSkuImage psi
        JOIN psi.productSku sku
        WHERE psi.skuCover = true
        AND sku.id IN :productSkuIds
    """)
    List<ProductSkuCoverProjection> findCoversByProductSkuIds(@Param("productSkuIds") List<Long> productSkuIds);

    List<ProductSkuImage> findByProductSkuIdOrderByIdAsc(Long productSkuId);

    Optional<ProductSkuImage> findByIdAndProductSkuId(Long id, Long productSkuId);

    Optional<ProductSkuImage> findFirstByProductSkuIdOrderByIdAsc(Long productSkuId);

    Optional<ProductSkuImage> findFirstByProductSkuProductIdOrderByIdAsc(Long productId);

    boolean existsByProductSkuId(Long productSkuId);

    boolean existsByProductSkuProductId(Long productId);

    @Modifying
    @Query("UPDATE ProductSkuImage i SET i.productCover = true WHERE i.id = :id")
    void markProductCover(@Param("id") Long id);

    @Modifying
    @Query("""
        UPDATE ProductSkuImage i
        SET i.productCover = false
        WHERE i.productCover = true
        AND i.productSku.id IN (SELECT s.id FROM ProductSku s WHERE s.product.id = :productId)
    """)
    void clearProductCover(@Param("productId") Long productId);

    @Modifying
    @Query("UPDATE ProductSkuImage i SET i.skuCover = true WHERE i.id = :id")
    void markSkuCover(@Param("id") Long id);

    @Modifying
    @Query("""
        UPDATE ProductSkuImage i
        SET i.skuCover = false
        WHERE i.productSku.id = :skuId
        AND i.skuCover = true
    """)
    void clearSkuCover(@Param("skuId") Long skuId);
}
