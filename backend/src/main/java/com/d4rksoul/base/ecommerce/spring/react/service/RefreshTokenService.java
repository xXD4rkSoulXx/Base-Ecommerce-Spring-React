package com.d4rksoul.base.ecommerce.spring.react.service;

import com.d4rksoul.base.ecommerce.spring.react.dto.result.RotateRefreshTokenResult;
import com.d4rksoul.base.ecommerce.spring.react.entity.RefreshToken;
import com.d4rksoul.base.ecommerce.spring.react.entity.User;
import com.d4rksoul.base.ecommerce.spring.react.exception.InvalidRefreshTokenException;
import com.d4rksoul.base.ecommerce.spring.react.exception.RefreshTokenExpiredException;
import com.d4rksoul.base.ecommerce.spring.react.exception.RefreshTokenReuseException;
import com.d4rksoul.base.ecommerce.spring.react.repository.RefreshTokenRepository;
import com.d4rksoul.base.ecommerce.spring.react.security.HashService;
import com.d4rksoul.base.ecommerce.spring.react.security.TokenService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenService tokenService;
    private final HashService hashService;

    @Transactional
    public void create(User user, String rawToken) {
        String hashedToken = hashService.hash(rawToken);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(hashedToken)
                .expiresAt(Instant.now().plus(30, ChronoUnit.DAYS))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public RotateRefreshTokenResult rotate(String rawOldToken) {
        String hashedOldToken = hashService.hash(rawOldToken);

        RefreshToken storedToken = refreshTokenRepository.findByToken(hashedOldToken)
                .orElseThrow(() -> new InvalidRefreshTokenException("Invalid session"));

        User user = storedToken.getUser();

        if (storedToken.isRevoked()) {
            refreshTokenRepository.deleteAllByUser(user);
            throw new RefreshTokenReuseException("Security breach detected. All sessions revoked.");
        }

        if (storedToken.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(storedToken);
            throw new RefreshTokenExpiredException("Session expired. Please log in again.");
        }

        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        String rawNewToken = tokenService.generateRefreshToken(user);
        this.create(user, rawNewToken);

        return new RotateRefreshTokenResult(user, rawNewToken);
    }

    @Transactional
    public void delete(String rawToken) {
        String hashedToken = hashService.hash(rawToken);

        refreshTokenRepository.deleteByToken(hashedToken);
    }

    @Transactional
    public void deleteAllByUser(User user) {
        refreshTokenRepository.deleteAllByUser(user);
    }
}
