package com.d4rksoul.base.ecommerce.spring.react.repository;

import com.d4rksoul.base.ecommerce.spring.react.dto.internal.ProductCoverResult;
import com.d4rksoul.base.ecommerce.spring.react.entity.ProductSkuImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductSkuImageRepository extends JpaRepository<ProductSkuImage, Long> {

    @Query("""
        SELECT new com.d4rksoul.base.ecommerce.spring.react.dto.internal.ProductCoverResult(
            psi.productSku.product.id,
            psi.path
        )
        FROM ProductSkuImage psi
        WHERE psi.cover = true
        AND psi.productSku.product.id IN :productIds
    """)
    List<ProductCoverResult> findCoverByProductIds(@Param("productIds") List<Long> productIds);
}
