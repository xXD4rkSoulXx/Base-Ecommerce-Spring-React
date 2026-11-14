package com.d4rksoul.base.ecommerce.spring.react.repository;

import com.d4rksoul.base.ecommerce.spring.react.entity.DiscountCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface DiscountCodeRepository
    extends JpaRepository<DiscountCode, Long>, JpaSpecificationExecutor<DiscountCode> {

    Optional<DiscountCode> findByCode(String code);

    boolean existsByCode(String code);
}
