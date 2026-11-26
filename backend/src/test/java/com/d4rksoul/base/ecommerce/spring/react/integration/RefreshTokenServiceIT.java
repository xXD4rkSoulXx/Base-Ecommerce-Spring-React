package com.d4rksoul.base.ecommerce.spring.react.integration;

import com.d4rksoul.base.ecommerce.spring.react.constant.Constants;
import com.d4rksoul.base.ecommerce.spring.react.dto.result.RotateRefreshTokenResult;
import com.d4rksoul.base.ecommerce.spring.react.entity.RefreshToken;
import com.d4rksoul.base.ecommerce.spring.react.entity.User;
import com.d4rksoul.base.ecommerce.spring.react.exception.InvalidRefreshTokenException;
import com.d4rksoul.base.ecommerce.spring.react.exception.RefreshTokenExpiredException;
import com.d4rksoul.base.ecommerce.spring.react.exception.RefreshTokenReuseException;
import com.d4rksoul.base.ecommerce.spring.react.mapper.RefreshTokenMapper;
import com.d4rksoul.base.ecommerce.spring.react.repository.RefreshTokenRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.UserRepository;
import com.d4rksoul.base.ecommerce.spring.react.security.HashService;
import com.d4rksoul.base.ecommerce.spring.react.security.TokenService;
import com.d4rksoul.base.ecommerce.spring.react.service.RefreshTokenService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@Import({RefreshTokenService.class, HashService.class, RefreshTokenMapper.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class RefreshTokenServiceIT extends AbstractIntegrationTest {

    private static final String NEW_RAW_TOKEN = "new-raw-token";

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HashService hashService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private TokenService tokenService;

    @AfterEach
    void deleteCommittedData() {
        jdbcTemplate.update("delete from refresh_tokens");
        jdbcTemplate.update("delete from users");
    }

    private User addCommittedUser() {
        return userRepository.save(TestData.user());
    }

    private void addToken(User user, String rawToken, Instant expiresAt, boolean revoked) {
        refreshTokenRepository.save(RefreshToken.builder()
            .user(user)
            .token(hashService.hash(rawToken))
            .expiresAt(expiresAt)
            .revoked(revoked)
            .build());
    }

    private void addValidToken(User user, String rawToken) {
        addToken(user, rawToken, Instant.now().plus(1, ChronoUnit.DAYS), false);
    }

    private int tokensOf(User user) {
        Integer count = jdbcTemplate.queryForObject(
            "select count(*) from refresh_tokens where user_id = ?", Integer.class, user.getId()
        );

        return count == null ? 0 : count;
    }

    private boolean existsInDatabase(String rawToken) {
        Integer count = jdbcTemplate.queryForObject(
            "select count(*) from refresh_tokens where token = ?",
            Integer.class,
            hashService.hash(rawToken)
        );

        return count != null && count > 0;
    }

    private boolean isRevokedInDatabase(String rawToken) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
            "select revoked from refresh_tokens where token = ?",
            Boolean.class,
            hashService.hash(rawToken)
        ));
    }

    @Test
    @DisplayName("rotate: revokes the old token, stores the new one hashed and returns it raw")
    void rotateValidToken() {
        User user = addCommittedUser();

        addValidToken(user, "old-token");

        given(tokenService.generateRefreshToken()).willReturn(NEW_RAW_TOKEN);

        RotateRefreshTokenResult result = refreshTokenService.rotate("old-token");

        Timestamp newExpiresAt = jdbcTemplate.queryForObject(
            "select expires_at from refresh_tokens where token = ?",
            Timestamp.class,
            hashService.hash(NEW_RAW_TOKEN)
        );

        assertThat(result.rawRefreshToken()).isEqualTo(NEW_RAW_TOKEN);
        assertThat(result.user().getId()).isEqualTo(user.getId());
        assertThat(isRevokedInDatabase("old-token")).isTrue();
        assertThat(existsInDatabase(NEW_RAW_TOKEN)).isTrue();
        assertThat(isRevokedInDatabase(NEW_RAW_TOKEN)).isFalse();
        assertThat(newExpiresAt).isNotNull();
        assertThat(newExpiresAt.toInstant())
            .isAfter(
                Instant.now()
                    .plus(Constants.REFRESH_TOKEN_TTL)
                    .minus(1, ChronoUnit.HOURS)
            );
    }

    @Test
    @DisplayName("rotate: an unknown token is rejected and nothing changes")
    void rotateUnknownToken() {
        User user = addCommittedUser();

        addValidToken(user, "existing-token");

        assertThatThrownBy(() -> refreshTokenService.rotate("unknown-token"))
            .isInstanceOf(InvalidRefreshTokenException.class);
        assertThat(tokensOf(user)).isEqualTo(1);
        assertThat(isRevokedInDatabase("existing-token")).isFalse();
    }

    @Test
    @DisplayName("rotate: reusing a revoked token deletes every session of that user, even with the exception")
    void rotateRevokedTokenDeletesAllSessionsOfTheUser() {
        User attacked = addCommittedUser();
        User other = addCommittedUser();

        addToken(
            attacked,
            "revoked-token",
            Instant.now().plus(1, ChronoUnit.DAYS),
            true
        );
        addValidToken(attacked, "other-session-of-attacked");
        addValidToken(other, "session-of-other");

        assertThatThrownBy(() -> refreshTokenService.rotate("revoked-token"))
            .isInstanceOf(RefreshTokenReuseException.class);
        assertThat(tokensOf(attacked)).isZero();
        assertThat(tokensOf(other)).isEqualTo(1);
    }

    @Test
    @DisplayName("rotate: an expired token is deleted, even with the exception")
    void rotateExpiredTokenDeletesIt() {
        User user = addCommittedUser();

        addToken(
            user,
            "expired-token",
            Instant.now().minus(1, ChronoUnit.DAYS),
            false
        );
        addValidToken(user, "valid-token");

        assertThatThrownBy(() -> refreshTokenService.rotate("expired-token"))
            .isInstanceOf(RefreshTokenExpiredException.class);
        assertThat(existsInDatabase("expired-token")).isFalse();
        assertThat(existsInDatabase("valid-token")).isTrue();
    }

    @Test
    @DisplayName("delete: removes only the given token")
    void deleteRemovesOnlyThatToken() {
        User user = addCommittedUser();

        addValidToken(user, "token-a");
        addValidToken(user, "token-b");

        refreshTokenService.delete("token-a");

        assertThat(existsInDatabase("token-a")).isFalse();
        assertThat(existsInDatabase("token-b")).isTrue();
    }
}
