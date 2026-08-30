package com.d4rksoul.base.ecommerce.spring.react.repository;

import com.d4rksoul.base.ecommerce.spring.react.dto.projection.ProductCoverProjection;
import com.d4rksoul.base.ecommerce.spring.react.entity.ProductSkuImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductSkuImageRepository extends JpaRepository<ProductSkuImage, Long> {

    @Query("""
        SELECT new com.d4rksoul.base.ecommerce.spring.react.dto.projection.ProductCoverProjection(
            p.id,
            psi.path
        )
        FROM ProductSkuImage psi
        JOIN psi.productSku sku
        JOIN sku.product p
        WHERE psi.cover = true
        AND p.id IN :productIds
    """)
    List<ProductCoverProjection> findCoverByProductIds(@Param("productIds") List<Long> productIds);
}
