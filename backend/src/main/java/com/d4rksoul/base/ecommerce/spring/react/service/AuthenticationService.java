package com.d4rksoul.base.ecommerce.spring.react.service;

import com.d4rksoul.base.ecommerce.spring.react.dto.internal.LoginResult;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.LoginRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.RegisterRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.UserResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.User;
import com.d4rksoul.base.ecommerce.spring.react.enums.UserRole;
import com.d4rksoul.base.ecommerce.spring.react.mapper.AuthenticationMapper;
import com.d4rksoul.base.ecommerce.spring.react.mapper.UserMapper;
import com.d4rksoul.base.ecommerce.spring.react.repository.RefreshTokenRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.UserRepository;
import com.d4rksoul.base.ecommerce.spring.react.util.EncryptionUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final UserRepository userRepository;
    private final AuthenticationMapper authenticationMapper;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtDecoder jwtDecoder;

    @Transactional
    public void register(RegisterRequest request) {
        log.info("Processing user registration for email: {}", request.email());
        if (userRepository.existsByEmail(request.email())) {
            log.warn("Registration failed: email {} already exists", request.email());
            throw new IllegalArgumentException("Email address is already registered");
        }

        User user = authenticationMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(UserRole.CUSTOMER);

        userRepository.save(user);
        log.info("User successfully registered with email: {}", request.email());
    }

    public LoginResult login(LoginRequest request) {
        log.info("Processing login authentication for email: {}", request.email());
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    log.warn("Authentication failed: email {} not found", request.email());

                    return new IllegalArgumentException("Invalid email or password");
                });

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            log.warn("Authentication failed: incorrect password for email {}", request.email());
            throw new IllegalArgumentException("Invalid email or password");
        }
        log.info("User {} successfully authenticated", request.email());

        String accessToken = tokenService.generateAccessToken(user);
        String refreshToken = tokenService.generateRefreshToken(user);
        refreshTokenService.createRefreshToken(user, refreshToken);

        UserResponse userResponse = userMapper.toDto(user);

        return new LoginResult(userResponse, accessToken, refreshToken);
    }

    public LoginResult refreshSession(final String rawOldRefreshToken) {
        log.info("Processing token refresh request");
        Jwt jwt = jwtDecoder.decode(rawOldRefreshToken);
        String email = jwt.getSubject();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid session owner"));

        String rawNewRefreshToken = refreshTokenService.rotateRefreshToken(rawOldRefreshToken, user);
        String newAccessToken = tokenService.generateAccessToken(user);

        UserResponse userResponse = userMapper.toDto(user);

        return new LoginResult(userResponse, newAccessToken, rawNewRefreshToken);
    }

    @Transactional
    public void logoutSession(final String rawRefreshToken) {
        log.info("Processing single-device logout session");

        try {
            String token = EncryptionUtil.hashToSHA256(rawRefreshToken);

            refreshTokenRepository.deleteByToken(token);

            log.info("Successfully deleted current refresh token from database. Other devices remain logged in.");
        } catch (Exception e) {
            log.warn("Logout warning: Could not clear token from DB, clearing browser cookie anyway.", e);
        }
    }


    @Transactional
    public void logoutAllDevices(final String email) {
        log.info("Processing global logout for user: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        refreshTokenRepository.deleteByUser(user);

        log.info("All device sessions revoked successfully for {}", email);
    }

}
