package com.d4rksoul.base.ecommerce.spring.react.mapper;

import com.d4rksoul.base.ecommerce.spring.react.entity.RefreshToken;
import com.d4rksoul.base.ecommerce.spring.react.entity.User;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class RefreshTokenMapper {

    public RefreshToken toEntity(User user, String hashedToken, Instant expiresAt) {
        return RefreshToken.builder()
                .user(user)
                .token(hashedToken)
                .expiresAt(expiresAt)
                .revoked(false)
                .build();
    }
}
