package com.d4rksoul.base.ecommerce.spring.react.controller;

import com.d4rksoul.base.ecommerce.spring.react.dto.result.LoginResult;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.LoginResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.LoginRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.RegisterRequest;
import com.d4rksoul.base.ecommerce.spring.react.exception.InvalidRefreshTokenException;
import com.d4rksoul.base.ecommerce.spring.react.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final Environment environment;
    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
        authenticationService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResult result = authenticationService.login(request);

        ResponseCookie refreshTokenCookie = buildRefreshTokenCookie(
                result.refreshToken(),
                30L * 24 * 60 * 60
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(new LoginResponse(result.user(), result.accessToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(
            @CookieValue(name = "refreshToken", required = false) String rawOldRefreshToken
    ) {
        if (rawOldRefreshToken == null) {
            throw new InvalidRefreshTokenException("No active session");
        }

        LoginResult result = authenticationService.refresh(rawOldRefreshToken);

        ResponseCookie newRefreshTokenCookie = buildRefreshTokenCookie(
                result.refreshToken(),
                30L * 24 * 60 * 60
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, newRefreshTokenCookie.toString())
                .body(new LoginResponse(result.user(), result.accessToken()));
    }

    @DeleteMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = "refreshToken", required = false) String rawRefreshToken
    ) {
        if (rawRefreshToken != null) {
            authenticationService.logout(rawRefreshToken);
        }

        ResponseCookie cleanCookie = buildRefreshTokenCookie("", 0);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cleanCookie.toString())
                .build();
    }

    @DeleteMapping("/logout-all")
    public ResponseEntity<Void> logoutAllByUser(@AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt) {
        String email = jwt.getSubject();

        authenticationService.logoutAllByUser(email);

        ResponseCookie cleanCookie = buildRefreshTokenCookie("", 0);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cleanCookie.toString())
                .build();
    }

    private ResponseCookie buildRefreshTokenCookie(String value, long maxAgeSeconds) {
        boolean isProd = environment.acceptsProfiles(Profiles.of("prod"));

        return ResponseCookie.from("refreshToken", value)
                .httpOnly(true)
                .secure(isProd)
                .path("/api/auth")
                .maxAge(maxAgeSeconds)
                .sameSite(isProd ? "None" : "Lax")
                .build();
    }
}
