package com.d4rksoul.base.ecommerce.spring.react.repository;

import com.d4rksoul.base.ecommerce.spring.react.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    @Query(value = """
        SELECT r FROM Review r 
        JOIN FETCH r.user 
        WHERE r.product.id = :productId
    """, countQuery = """
        SELECT COUNT(r) FROM Review r 
        WHERE r.product.id = :productId
    """)
    Page<Review> findByProductId(@Param("productId") Long productId, Pageable pageable);
}
