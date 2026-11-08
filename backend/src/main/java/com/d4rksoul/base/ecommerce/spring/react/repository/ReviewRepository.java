package com.d4rksoul.base.ecommerce.spring.react.repository;

import com.d4rksoul.base.ecommerce.spring.react.entity.Review;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long>, JpaSpecificationExecutor<Review> {

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    @Query(value = """
        SELECT r FROM Review r 
        JOIN FETCH r.user 
        WHERE r.product.id = :productId
        AND r.hidden = false
    """, countQuery = """
        SELECT COUNT(r) FROM Review r 
        WHERE r.product.id = :productId
        AND r.hidden = false
    """)
    Page<Review> findByProductId(@Param("productId") Long productId, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"user", "product"})
    @NonNull
    Page<Review> findAll(@NonNull Specification<Review> specification, @NonNull Pageable pageable);
}
