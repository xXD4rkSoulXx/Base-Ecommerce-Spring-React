package com.d4rksoul.base.ecommerce.repository;

import com.d4rksoul.base.ecommerce.domain.Discount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiscountRepository extends JpaRepository<Discount, Long> {
}
