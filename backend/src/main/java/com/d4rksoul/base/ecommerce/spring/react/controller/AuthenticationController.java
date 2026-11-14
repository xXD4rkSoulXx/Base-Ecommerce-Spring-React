package com.d4rksoul.base.ecommerce.spring.react.controller;

import com.d4rksoul.base.ecommerce.spring.react.constant.Constants;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.ChangePasswordRequest;
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
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

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

        return buildLoginResponse(result);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(
        @CookieValue(name = "refreshToken", required = false) String rawOldRefreshToken
    ) {
        if (rawOldRefreshToken == null) {
            throw new InvalidRefreshTokenException("No active session");
        }

        LoginResult result = authenticationService.refresh(rawOldRefreshToken);

        return buildLoginResponse(result);
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authenticationService.changePassword(request);

        return buildSessionClearedResponse();
    }

    @DeleteMapping("/logout")
    public ResponseEntity<Void> logout(
        @CookieValue(name = "refreshToken", required = false) String rawRefreshToken
    ) {
        if (rawRefreshToken != null) {
            authenticationService.logout(rawRefreshToken);
        }

        return buildSessionClearedResponse();
    }

    @DeleteMapping("/logout-all")
    public ResponseEntity<Void> logoutAllByUser() {
        authenticationService.logoutAllByUser();

        return buildSessionClearedResponse();
    }

    private ResponseEntity<LoginResponse> buildLoginResponse(LoginResult result) {
        ResponseCookie refreshTokenCookie = buildRefreshTokenCookie(
            result.refreshToken(), Constants.REFRESH_TOKEN_TTL
        );

        LoginResponse body = LoginResponse.builder()
            .user(result.user())
            .accessToken(result.accessToken())
            .build();

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
            .body(body);
    }

    private ResponseEntity<Void> buildSessionClearedResponse() {
        ResponseCookie cleanCookie = buildRefreshTokenCookie("", Duration.ZERO);

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, cleanCookie.toString())
            .build();
    }

    private ResponseCookie buildRefreshTokenCookie(String value, Duration maxAge) {
        boolean isProd = environment.acceptsProfiles(Profiles.of("prod"));

        return ResponseCookie.from("refreshToken", value)
            .httpOnly(true)
            .secure(isProd)
            .path("/api/auth")
            .maxAge(maxAge)
            .sameSite(isProd ? "None" : "Lax")
            .build();
    }
}
