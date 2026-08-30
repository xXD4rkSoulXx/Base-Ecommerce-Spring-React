package com.d4rksoul.base.ecommerce.spring.react.repository;

import com.d4rksoul.base.ecommerce.spring.react.entity.Product;
import com.d4rksoul.base.ecommerce.spring.react.enums.ProductCategory;
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
}
