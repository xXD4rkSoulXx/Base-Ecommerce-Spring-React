package com.d4rksoul.base.ecommerce.spring.react.repository;

import com.d4rksoul.base.ecommerce.spring.react.dto.projection.RatingStatsProjection;
import com.d4rksoul.base.ecommerce.spring.react.entity.Product;
import com.d4rksoul.base.ecommerce.spring.react.enums.FeaturedSlot;
import com.d4rksoul.base.ecommerce.spring.react.enums.OrderStatus;
import com.d4rksoul.base.ecommerce.spring.react.enums.ProductCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    @Query("""
        SELECT new com.d4rksoul.base.ecommerce.spring.react.dto.projection.RatingStatsProjection(
            COALESCE(AVG(r.rating), 0.0),
            COUNT(r)
        )
        FROM Review r
        WHERE r.product.id = :productId
        AND r.hidden = false
    """)
    Optional<RatingStatsProjection> findRatingStatsByProductId(@Param("productId") Long productId);

    @EntityGraph(attributePaths = {"productSkus"})
    @Query("""
        SELECT p
        FROM Product p
        WHERE p.id = :id
        AND p.active = true
        AND EXISTS (
            SELECT 1
            FROM ProductSku activeSku
            WHERE activeSku.product = p AND activeSku.active = true
        )
    """)
    Optional<Product> findVisibleDetailsById(@Param("id") Long id);

    Optional<Product> findByFeaturedSlot(FeaturedSlot featuredSlot);

    boolean existsByRef(String ref);

    boolean existsByRefAndIdNot(String ref, Long id);

    @Query(
            value = """
        SELECT p
        FROM Product p
        JOIN p.productSkus sku
        JOIN sku.orderItems oi
        JOIN oi.order o
        WHERE o.status = :status
        AND (:category IS NULL OR p.category = :category)
        AND p.active = true
        AND EXISTS (
            SELECT 1
            FROM ProductSku activeSku
            WHERE activeSku.product = p AND activeSku.active = true
        )
        GROUP BY p
        ORDER BY SUM(oi.quantity) DESC
    """,
            countQuery = """
        SELECT COUNT(p)
        FROM Product p
        WHERE p.id IN (
            SELECT p2.id
            FROM Product p2
            JOIN p2.productSkus sku
            JOIN sku.orderItems oi
            JOIN oi.order o
            WHERE o.status = :status
            AND (:category IS NULL OR p2.category = :category)
            AND p2.active = true
            AND EXISTS (
                SELECT 1
                FROM ProductSku activeSku
                WHERE activeSku.product = p2 AND activeSku.active = true
            )
            GROUP BY p2.id
        )
    """
    )
    Page<Product> findBestSellersPage(
            @Param("status") OrderStatus status,
            @Param("category") ProductCategory category,
            Pageable pageable
    );

    @Query(
            value = """
        SELECT p
        FROM Product p
        JOIN p.reviews r
        WHERE (:category IS NULL OR p.category = :category)
        AND r.hidden = false
        AND p.active = true
        AND EXISTS (
            SELECT 1
            FROM ProductSku activeSku
            WHERE activeSku.product = p AND activeSku.active = true
        )
        GROUP BY p
        HAVING COUNT(r) >= :minReviews
        ORDER BY AVG(r.rating) DESC
    """,
            countQuery = """
        SELECT COUNT(p)
        FROM Product p
        WHERE p.id IN (
            SELECT p2.id
            FROM Product p2
            JOIN p2.reviews r
            WHERE (:category IS NULL OR p2.category = :category)
            AND r.hidden = false
            AND p2.active = true
            AND EXISTS (
                SELECT 1
                FROM ProductSku activeSku
                WHERE activeSku.product = p2 AND activeSku.active = true
            )
            GROUP BY p2.id
            HAVING COUNT(r) >= :minReviews
        )
    """
    )
    Page<Product> findTopRatedPage(
            @Param("minReviews") long minReviews,
            @Param("category") ProductCategory category,
            Pageable pageable
    );
}
