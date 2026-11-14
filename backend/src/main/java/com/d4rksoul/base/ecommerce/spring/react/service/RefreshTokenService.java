package com.d4rksoul.base.ecommerce.spring.react.service;

import com.d4rksoul.base.ecommerce.spring.react.constant.Constants;
import com.d4rksoul.base.ecommerce.spring.react.dto.result.RotateRefreshTokenResult;
import com.d4rksoul.base.ecommerce.spring.react.entity.RefreshToken;
import com.d4rksoul.base.ecommerce.spring.react.entity.User;
import com.d4rksoul.base.ecommerce.spring.react.exception.InvalidRefreshTokenException;
import com.d4rksoul.base.ecommerce.spring.react.exception.RefreshTokenException;
import com.d4rksoul.base.ecommerce.spring.react.exception.RefreshTokenExpiredException;
import com.d4rksoul.base.ecommerce.spring.react.exception.RefreshTokenReuseException;
import com.d4rksoul.base.ecommerce.spring.react.mapper.RefreshTokenMapper;
import com.d4rksoul.base.ecommerce.spring.react.repository.RefreshTokenRepository;
import com.d4rksoul.base.ecommerce.spring.react.security.HashService;
import com.d4rksoul.base.ecommerce.spring.react.security.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenService tokenService;
    private final HashService hashService;
    private final RefreshTokenMapper refreshTokenMapper;

    public void create(User user, String rawToken) {
        String hashedToken = hashService.hash(rawToken);
        Instant expiresAt = Instant.now().plus(Constants.REFRESH_TOKEN_TTL);

        RefreshToken refreshToken = refreshTokenMapper.toEntity(user, hashedToken, expiresAt);

        refreshTokenRepository.save(refreshToken);
    }

    @Transactional(noRollbackFor = RefreshTokenException.class)
    public RotateRefreshTokenResult rotate(String rawOldToken) {
        String hashedOldToken = hashService.hash(rawOldToken);

        RefreshToken storedToken = refreshTokenRepository.findByToken(hashedOldToken)
            .orElseThrow(() -> new InvalidRefreshTokenException("Invalid session"));

        if (storedToken.isRevoked()) {
            refreshTokenRepository.deleteAllByUser(storedToken.getUser());
            throw new RefreshTokenReuseException("Security breach detected. All sessions revoked.");
        }

        if (storedToken.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(storedToken);
            throw new RefreshTokenExpiredException("Session expired. Please log in again.");
        }

        int updatedRows = refreshTokenRepository.revoke(storedToken.getId());
        if (updatedRows == 0) {
            refreshTokenRepository.deleteAllByUser(storedToken.getUser());
            throw new RefreshTokenReuseException("Security breach detected. All sessions revoked.");
        }

        String rawNewToken = tokenService.generateRefreshToken();

        create(storedToken.getUser(), rawNewToken);

        return refreshTokenMapper.toRotateResult(storedToken.getUser(), rawNewToken);
    }

    @Transactional
    public void delete(String rawToken) {
        String hashedToken = hashService.hash(rawToken);

        refreshTokenRepository.deleteByToken(hashedToken);
    }
}
