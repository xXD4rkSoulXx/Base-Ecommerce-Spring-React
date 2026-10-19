package com.d4rksoul.base.ecommerce.spring.react.repository;

import com.d4rksoul.base.ecommerce.spring.react.entity.ProductSku;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductSkuRepository extends JpaRepository<ProductSku, Long> {

    @Modifying
    @Query("update ProductSku s set s.stock = s.stock + :quantity where s.id = :id")
    void incrementStock(@Param("id") Long id, @Param("quantity") int quantity);

    @Modifying
    @Query("update ProductSku s set s.stock = s.stock - :quantity where s.id = :id and s.stock >= :quantity")
    int decrementStock(@Param("id") Long id, @Param("quantity") int quantity);
}
