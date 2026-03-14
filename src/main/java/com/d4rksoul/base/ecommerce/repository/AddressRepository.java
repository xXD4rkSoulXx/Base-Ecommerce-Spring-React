package com.d4rksoul.base.ecommerce.repository;

import com.d4rksoul.base.ecommerce.domain.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
}
