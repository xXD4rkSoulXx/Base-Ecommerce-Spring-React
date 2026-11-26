package com.d4rksoul.base.ecommerce.spring.react.security;

import com.d4rksoul.base.ecommerce.spring.react.config.BackendProperties;
import com.d4rksoul.base.ecommerce.spring.react.constant.Constants;
import com.d4rksoul.base.ecommerce.spring.react.entity.User;
import com.d4rksoul.base.ecommerce.spring.react.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    private static final String BACKEND_URL = "http://localhost:8080";
    private static final String SIGNED_TOKEN = "signed-token";

    @Mock
    private JwtEncoder jwtEncoder;

    private TokenService service;

    @BeforeEach
    void setUp() {
        service = new TokenService(jwtEncoder, new BackendProperties(BACKEND_URL));
    }

    @Test
    @DisplayName("access token: carries the user's identity, role and permissions and returns the signed value")
    void accessTokenCarriesUserData() {
        User user = user(UserRole.SUPPORT);

        stubEncoder();

        String token = service.generateAccessToken(user);

        JwtClaimsSet claims = captureClaims();

        assertThat(token).isEqualTo(SIGNED_TOKEN);
        assertThat(claims.getSubject()).isEqualTo("ana@example.com");
        assertThat(claims.getIssuer()).hasToString(BACKEND_URL);
        assertThat(claims.<String>getClaim("firstName")).isEqualTo("Ana");
        assertThat(claims.<String>getClaim("lastName")).isEqualTo("Silva");
        assertThat(claims.<String>getClaim("role")).isEqualTo("SUPPORT");
        assertThat(claims.getClaimAsStringList("permissions"))
            .containsExactlyInAnyOrderElementsOf(
                UserRole.SUPPORT.getPermissions().stream().map(Enum::name).toList()
            );
    }

    @Test
    @DisplayName("access token: a customer gets an empty list of permissions")
    void accessTokenOfCustomerHasNoPermissions() {
        stubEncoder();

        service.generateAccessToken(user(UserRole.CUSTOMER));

        JwtClaimsSet claims = captureClaims();

        assertThat(claims.<String>getClaim("role")).isEqualTo("CUSTOMER");
        assertThat(claims.getClaimAsStringList("permissions")).isEmpty();
    }

    @Test
    @DisplayName("access token: expires after the configured time to live")
    void accessTokenExpiresAfterTimeToLive() {
        stubEncoder();

        service.generateAccessToken(user(UserRole.CUSTOMER));

        JwtClaimsSet claims = captureClaims();

        Instant issuedAt = claims.getIssuedAt();
        Instant expiresAt = claims.getExpiresAt();

        assertThat(issuedAt).isNotNull();
        assertThat(expiresAt).isNotNull();
        assertThat(Duration.between(issuedAt, expiresAt)).isEqualTo(Constants.ACCESS_TOKEN_TTL);
    }

    @Test
    @DisplayName("refresh token: has 32 random bytes encoded in URL-safe Base64 without padding")
    void refreshTokenIsUrlSafe() {
        String token = service.generateRefreshToken();

        assertThat(token)
            .hasSize(43)
            .matches("[A-Za-z0-9_-]+");
    }

    @Test
    @DisplayName("refresh token: two tokens in a row are never equal")
    void refreshTokensAreUnique() {
        String first = service.generateRefreshToken();
        String second = service.generateRefreshToken();

        assertThat(first).isNotEqualTo(second);
    }

    private void stubEncoder() {
        Jwt jwt = Jwt.withTokenValue(SIGNED_TOKEN).header("alg", "RS256")
            .claim("sub", "ana@example.com").build();

        when(jwtEncoder.encode(org.mockito.ArgumentMatchers.any(JwtEncoderParameters.class)))
            .thenReturn(jwt);
    }

    private JwtClaimsSet captureClaims() {
        ArgumentCaptor<JwtEncoderParameters> captor =
            ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(jwtEncoder).encode(captor.capture());

        return captor.getValue().getClaims();
    }

    private User user(UserRole role) {
        return User.builder()
            .email("ana@example.com")
            .firstName("Ana")
            .lastName("Silva")
            .role(role)
            .build();
    }
}
