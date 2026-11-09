package com.d4rksoul.base.ecommerce.spring.react.repository;

import com.d4rksoul.base.ecommerce.spring.react.entity.User;
import com.d4rksoul.base.ecommerce.spring.react.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    Optional<User> findByIdAndRole(Long id, UserRole role);

    List<User> findByRoleNotOrderByRoleAscEmailAsc(UserRole role);

    long countByRole(UserRole role);

    @Query("""
        SELECT COUNT(u)
        FROM User u
        WHERE u.role = :role
        AND u.createdAt >= :start
        AND u.createdAt < :end
    """)
    long countByRoleInPeriod(
            @Param("role") UserRole role,
            @Param("start") Instant start,
            @Param("end") Instant end
    );
}
