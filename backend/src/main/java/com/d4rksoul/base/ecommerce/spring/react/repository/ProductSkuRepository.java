package com.d4rksoul.base.ecommerce.spring.react.repository;

import com.d4rksoul.base.ecommerce.spring.react.dto.projection.ProductStockProjection;
import com.d4rksoul.base.ecommerce.spring.react.entity.ProductSku;
import com.d4rksoul.base.ecommerce.spring.react.enums.ProductColor;
import com.d4rksoul.base.ecommerce.spring.react.enums.ProductSize;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductSkuRepository
    extends JpaRepository<ProductSku, Long>, JpaSpecificationExecutor<ProductSku> {

    @Override
    @EntityGraph(attributePaths = "product")
    @NonNull
    Page<ProductSku> findAll(@NonNull Specification<ProductSku> specification, @NonNull Pageable pageable);

    List<ProductSku> findByProductIdOrderByIdAsc(Long productId);

    Optional<ProductSku> findByIdAndProductId(Long id, Long productId);

    Optional<ProductSku> findByIdAndActiveTrueAndProductActiveTrue(Long id);

    Optional<ProductSku> findByProductIdAndSizeAndColor(
        Long productId, ProductSize size, ProductColor color
    );

    @Query("""
        SELECT new com.d4rksoul.base.ecommerce.spring.react.dto.projection.ProductStockProjection(
            sku.product.id,
            SUM(sku.stock),
            COUNT(sku),
            SUM(CASE WHEN sku.stock <= :threshold THEN 1 ELSE 0 END)
        )
        FROM ProductSku sku
        WHERE sku.product.id IN :productIds AND sku.active = true
        GROUP BY sku.product.id
    """)
    List<ProductStockProjection> summarizeStockByProductIds(
        @Param("productIds") List<Long> productIds, @Param("threshold") int threshold
    );

    @Modifying(flushAutomatically = true)
    @Query("update ProductSku s set s.stock = s.stock + :quantity where s.id = :id")
    void incrementStock(@Param("id") Long id, @Param("quantity") int quantity);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update ProductSku s set s.stock = s.stock - :quantity where s.id = :id and s.stock >= :quantity")
    int decrementStock(@Param("id") Long id, @Param("quantity") int quantity);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update ProductSku s set s.stock = s.stock + :delta where s.id = :id and s.stock + :delta >= 0")
    int adjustStock(@Param("id") Long id, @Param("delta") int delta);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update ProductSku s set s.stock = :stock where s.id = :id")
    int setStock(@Param("id") Long id, @Param("stock") int stock);
}
