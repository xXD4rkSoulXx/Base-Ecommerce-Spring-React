package com.d4rksoul.base.ecommerce.spring.react.service;

import com.d4rksoul.base.ecommerce.spring.react.constant.Constants;
import com.d4rksoul.base.ecommerce.spring.react.dto.result.RotateRefreshTokenResult;
import com.d4rksoul.base.ecommerce.spring.react.entity.RefreshToken;
import com.d4rksoul.base.ecommerce.spring.react.entity.User;
import com.d4rksoul.base.ecommerce.spring.react.exception.InvalidRefreshTokenException;
import com.d4rksoul.base.ecommerce.spring.react.exception.RefreshTokenExpiredException;
import com.d4rksoul.base.ecommerce.spring.react.exception.RefreshTokenReuseException;
import com.d4rksoul.base.ecommerce.spring.react.mapper.RefreshTokenMapper;
import com.d4rksoul.base.ecommerce.spring.react.repository.RefreshTokenRepository;
import com.d4rksoul.base.ecommerce.spring.react.security.HashService;
import com.d4rksoul.base.ecommerce.spring.react.security.TokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    private static final String OLD_TOKEN = "old-token";

    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private TokenService tokenService;
    @Spy
    private HashService hashService = new HashService();
    @Spy
    private RefreshTokenMapper refreshTokenMapper = new RefreshTokenMapper();

    @InjectMocks
    private RefreshTokenService service;

    private final User user = User.builder()
        .id(1L)
        .email("ana@mail.com")
        .build();

    @Test
    @DisplayName("create: stores only the hash of the token, with the configured lifetime")
    void createStoresOnlyTheHash() {
        Instant before = Instant.now();

        service.create(user, "raw-token");

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        RefreshToken saved = captor.getValue();

        assertThat(saved.getToken()).isNotEqualTo("raw-token");
        assertThat(saved.getToken()).isEqualTo(hashService.hash("raw-token"));
        assertThat(saved.getUser()).isSameAs(user);
        assertThat(saved.isRevoked()).isFalse();
        assertThat(saved.getExpiresAt()).isAfterOrEqualTo(before.plus(Constants.REFRESH_TOKEN_TTL));
    }

    @Test
    @DisplayName("rotate: rejects a token that does not exist")
    void rotateRejectsUnknownToken() {
        when(refreshTokenRepository.findByToken(hashService.hash(OLD_TOKEN)))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.rotate(OLD_TOKEN))
            .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    @DisplayName("rotate: treats an already revoked token as theft and logs out every session")
    void rotateTreatsRevokedTokenAsReuse() {
        givenStoredToken(Instant.now().plus(Duration.ofDays(1)), true);

        assertThatThrownBy(() -> service.rotate(OLD_TOKEN))
            .isInstanceOf(RefreshTokenReuseException.class);

        verify(refreshTokenRepository).deleteAllByUser(user);
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("rotate: rejects an expired token and deletes it")
    void rotateRejectsExpiredToken() {
        RefreshToken stored = givenStoredToken(
            Instant.now().minusSeconds(60), false
        );

        assertThatThrownBy(() -> service.rotate(OLD_TOKEN))
            .isInstanceOf(RefreshTokenExpiredException.class);

        verify(refreshTokenRepository).delete(stored);
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("rotate: treats losing a concurrent rotation as theft and logs out every session")
    void rotateTreatsLostRaceAsReuse() {
        givenStoredToken(Instant.now().plus(Duration.ofDays(1)), false);

        when(refreshTokenRepository.revoke(10L)).thenReturn(0);

        assertThatThrownBy(() -> service.rotate(OLD_TOKEN))
            .isInstanceOf(RefreshTokenReuseException.class);

        verify(refreshTokenRepository).deleteAllByUser(user);
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("rotate: revokes the old token and issues a new one")
    void rotateIssuesNewTokenAndRevokesOld() {
        givenStoredToken(Instant.now().plus(Duration.ofDays(1)), false);

        when(refreshTokenRepository.revoke(10L)).thenReturn(1);
        when(tokenService.generateRefreshToken()).thenReturn("new-token");

        RotateRefreshTokenResult result = service.rotate(OLD_TOKEN);

        assertThat(result.rawRefreshToken()).isEqualTo("new-token");
        assertThat(result.user()).isSameAs(user);

        verify(refreshTokenRepository).revoke(10L);

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());

        assertThat(captor.getValue().getToken()).isEqualTo(hashService.hash("new-token"));
        assertThat(captor.getValue().isRevoked()).isFalse();
    }

    @Test
    @DisplayName("delete: removes the token by its hash")
    void deleteRemovesByHash() {
        service.delete("raw-token");

        verify(refreshTokenRepository).deleteByToken(hashService.hash("raw-token"));
    }

    private RefreshToken givenStoredToken(Instant expiresAt, boolean revoked) {
        RefreshToken stored = RefreshToken.builder()
            .id(10L)
            .user(user)
            .token(hashService.hash(OLD_TOKEN))
            .expiresAt(expiresAt)
            .revoked(revoked)
            .build();

        when(refreshTokenRepository.findByToken(hashService.hash(OLD_TOKEN)))
            .thenReturn(Optional.of(stored));

        return stored;
    }
}
