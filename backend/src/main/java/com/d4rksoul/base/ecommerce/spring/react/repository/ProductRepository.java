package com.d4rksoul.base.ecommerce.spring.react.repository;

import com.d4rksoul.base.ecommerce.spring.react.entity.Product;
import com.d4rksoul.base.ecommerce.spring.react.enums.OrderStatus;
import com.d4rksoul.base.ecommerce.spring.react.enums.ProductCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    @EntityGraph(attributePaths = {"productSkus"})
    Optional<Product> findDetailsById(Long id);

    @Query("""
        SELECT COALESCE(AVG(r.rating), 0.0), COUNT(r)
        FROM Review r
        WHERE r.product.id = :productId
    """)
    List<Object[]> getRatingStatsByProductId(@Param("productId") Long productId);

    List<Product> findByCategoryAndIdNot(ProductCategory category, Long id, Pageable pageable);

    List<Product> findByFeaturedTrue(Pageable pageable);

    List<Product> findByDiscountPercentageGreaterThan(int discountPercentage, Pageable pageable);

    @Query(
            value = """
        SELECT p
        FROM Product p
        JOIN p.productSkus sku
        JOIN sku.orderItems oi
        JOIN oi.order o
        WHERE o.status = :status
        AND (:category IS NULL OR p.category = :category)
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
