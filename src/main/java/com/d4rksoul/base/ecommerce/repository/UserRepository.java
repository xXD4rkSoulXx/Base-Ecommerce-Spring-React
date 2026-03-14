package com.d4rksoul.base.ecommerce.repository;

import com.d4rksoul.base.ecommerce.domain.User;
import com.d4rksoul.base.ecommerce.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<Boolean> existsByEmail(String email);
    List<User> findAllByRole(UserRole role);
}
