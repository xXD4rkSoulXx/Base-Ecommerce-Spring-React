package com.d4rksoul.base.ecommerce.spring.react.service;

import com.d4rksoul.base.ecommerce.spring.react.entity.RefreshToken;
import com.d4rksoul.base.ecommerce.spring.react.entity.User;
import com.d4rksoul.base.ecommerce.spring.react.repository.RefreshTokenRepository;
import com.d4rksoul.base.ecommerce.spring.react.util.EncryptionUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenService tokenService;

    @Transactional
    public void createRefreshToken(final User user, final String rawToken) {
        String token = EncryptionUtil.hashToSHA256(rawToken);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(token)
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public String rotateRefreshToken(final String rawOldToken, final User user) {
        String oldToken = EncryptionUtil.hashToSHA256(rawOldToken);

        RefreshToken storedToken = refreshTokenRepository.findByToken(oldToken)
                .orElseThrow(() -> {
                    log.warn("Security Alert: Reused or non-existent refresh token hash attempted!");
                    refreshTokenRepository.deleteByUser(user);

                    return new IllegalArgumentException("Invalid session");
                });

        if (storedToken.isRevoked()) {
            log.error("CRITICAL SECURITY ALERT: Refresh token reuse detected for user: {}! Potential breach.", user.getEmail());
            refreshTokenRepository.deleteByUser(user);
            throw new IllegalStateException("Security breach detected. All sessions revoked.");
        }

        if (storedToken.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(storedToken);
            throw new IllegalArgumentException("Session expired. Please log in again.");
        }

        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        String rawNewToken = tokenService.generateRefreshToken(user);
        this.createRefreshToken(user, rawNewToken);
        log.info("Refresh token successfully rotated for user: {}", user.getEmail());

        return rawNewToken;
    }
}
