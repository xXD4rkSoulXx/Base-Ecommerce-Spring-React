package com.d4rksoul.base.ecommerce.spring.react.service;

import com.d4rksoul.base.ecommerce.spring.react.dto.internal.LoginResult;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.LoginRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.RegisterRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.UserResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.RefreshToken;
import com.d4rksoul.base.ecommerce.spring.react.entity.User;
import com.d4rksoul.base.ecommerce.spring.react.enums.UserRole;
import com.d4rksoul.base.ecommerce.spring.react.exception.EmailAlreadyExistsException;
import com.d4rksoul.base.ecommerce.spring.react.exception.InvalidCredentialsException;
import com.d4rksoul.base.ecommerce.spring.react.exception.UserNotFoundException;
import com.d4rksoul.base.ecommerce.spring.react.mapper.AuthenticationMapper;
import com.d4rksoul.base.ecommerce.spring.react.mapper.UserMapper;
import com.d4rksoul.base.ecommerce.spring.react.repository.UserRepository;
import com.d4rksoul.base.ecommerce.spring.react.security.TokenService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    @Transactional
    public void register(RegisterRequest request) {
        log.info("Processing user registration for email: {}", request.email());
        if (userRepository.existsByEmail(request.email())) {
            log.warn("Registration failed: email {} already exists", request.email());
            throw new EmailAlreadyExistsException("Email address is already registered");
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

                    return new InvalidCredentialsException("Invalid email or password");
                });

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            log.warn("Authentication failed: incorrect password for email {}", request.email());
            throw new InvalidCredentialsException("Invalid email or password");
        }
        log.info("User {} successfully authenticated", request.email());

        String accessToken = tokenService.generateAccessToken(user);
        String refreshToken = tokenService.generateRefreshToken(user);
        refreshTokenService.saveRefreshToken(user, refreshToken);

        UserResponse userResponse = userMapper.toDto(user);

        return new LoginResult(userResponse, accessToken, refreshToken);
    }

    public LoginResult refreshSession(String oldRefreshToken) {
        log.info("Processing token refresh request");
        RefreshToken token = tokenService.validateRefreshToken(oldRefreshToken);
        User user = token.getUser();

        String newRefreshToken = refreshTokenService.rotateRefreshToken(oldRefreshToken, user);
        String accessToken = tokenService.generateAccessToken(user);

        UserResponse userResponse = userMapper.toDto(user);

        return new LoginResult(userResponse, accessToken, newRefreshToken);
    }

    @Transactional
    public void logoutSession(String rawRefreshToken) {
        log.info("Processing single-device logout session");
        try {
            refreshTokenService.deleteByToken(rawRefreshToken);
            log.info("Successfully deleted current refresh token from database. Other devices remain logged in.");
        } catch (DataAccessException e) {
            log.warn("Logout warning: Could not clear token from DB, clearing browser cookie anyway.", e);
        }
    }


    @Transactional
    public void logoutAllDevices(String email) {
        log.info("Processing global logout for user: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        refreshTokenService.deleteAllByUser(user);
        log.info("All device sessions revoked successfully for {}", email);
    }
}
