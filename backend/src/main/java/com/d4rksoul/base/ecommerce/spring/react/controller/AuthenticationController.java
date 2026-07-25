package com.d4rksoul.base.ecommerce.spring.react.controller;

import com.d4rksoul.base.ecommerce.spring.react.dto.internal.LoginResult;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.LoginResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.LoginRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.RegisterRequest;
import com.d4rksoul.base.ecommerce.spring.react.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
@Slf4j
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    @Value("${IS_PROD}")
    private boolean isProd;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
        log.info("HTTP POST Request: Registering new user with email: {}", request.email());
        authenticationService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("HTTP POST Request: User login attempt for email: {}", request.email());
        LoginResult result = authenticationService.login(request);

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", result.refreshToken())
                .httpOnly(true)
                .secure(isProd)
                .path("/api/auth")
                .maxAge(30L * 24 * 60 * 60)
                .sameSite(isProd ? "None" : "Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(new LoginResponse(result.user(), result.accessToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@CookieValue(name = "refreshToken") String rawOldRefreshToken) {
        log.info("HTTP POST Request: Refresh session");
        LoginResult result = authenticationService.refreshSession(rawOldRefreshToken);

        ResponseCookie newRefreshTokenCookie = ResponseCookie.from("refreshToken", result.refreshToken())
                .httpOnly(true)
                .secure(isProd)
                .path("/api/auth")
                .maxAge(30L * 24 * 60 * 60)
                .sameSite(isProd ? "None" : "Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, newRefreshTokenCookie.toString())
                .body(new LoginResponse(result.user(), result.accessToken()));
    }

    @DeleteMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(name = "refreshToken") String rawRefreshToken) {
        log.info("HTTP DELETE Request: Logout session");
        if (rawRefreshToken != null) {
            authenticationService.logout(rawRefreshToken);
        }

        ResponseCookie cleanCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(isProd)
                .path("/api/auth")
                .maxAge(0)
                .sameSite(isProd ? "None" : "Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cleanCookie.toString())
                .build();
    }

    @DeleteMapping("/logout-all")
    public ResponseEntity<Void> logoutAll(@AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt) {
        log.info("HTTP DELETE Request: Logout all sessions");
        String email = jwt.getSubject();

        authenticationService.logoutAllDevices(email);

        ResponseCookie cleanCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(isProd)
                .path("/api/auth")
                .maxAge(0)
                .sameSite(isProd ? "None" : "Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cleanCookie.toString())
                .build();
    }
}
