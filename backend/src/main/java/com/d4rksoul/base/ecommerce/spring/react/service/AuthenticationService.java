package com.d4rksoul.base.ecommerce.spring.react.service;

import com.d4rksoul.base.ecommerce.spring.react.dto.request.ChangePasswordRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.result.LoginResult;
import com.d4rksoul.base.ecommerce.spring.react.dto.result.RotateRefreshTokenResult;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.LoginRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.RegisterRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.UserResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.User;
import com.d4rksoul.base.ecommerce.spring.react.exception.EmailAlreadyExistsException;
import com.d4rksoul.base.ecommerce.spring.react.exception.InvalidCredentialsException;
import com.d4rksoul.base.ecommerce.spring.react.exception.InvalidPasswordChangeException;
import com.d4rksoul.base.ecommerce.spring.react.mapper.AuthenticationMapper;
import com.d4rksoul.base.ecommerce.spring.react.mapper.UserMapper;
import com.d4rksoul.base.ecommerce.spring.react.repository.RefreshTokenRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.UserRepository;
import com.d4rksoul.base.ecommerce.spring.react.security.TokenService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenService tokenService;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationMapper authenticationMapper;
    private final UserMapper userMapper;

    private String dummyPasswordHash;

    @PostConstruct
    void init() {
        dummyPasswordHash = passwordEncoder.encode(UUID.randomUUID().toString());
    }

    @Transactional
    public void register(RegisterRequest request) {
        boolean emailAlreadyRegistered = userRepository.existsByEmail(request.email());
        if (emailAlreadyRegistered) {
            throw new EmailAlreadyExistsException("Email address is already registered");
        }

        String encodedPassword = passwordEncoder.encode(request.password());
        User user = authenticationMapper.toEntity(request, encodedPassword);

        userRepository.save(user);
    }

    public LoginResult login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email()).orElse(null);

        String storedHash = user != null ? user.getPassword() : dummyPasswordHash;
        boolean passwordMatches = passwordEncoder.matches(request.password(), storedHash);

        if (user == null || !passwordMatches) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String accessToken = tokenService.generateAccessToken(user);
        String refreshToken = tokenService.generateRefreshToken();
        refreshTokenService.create(user, refreshToken);

        UserResponse userResponse = userMapper.toDto(user);

        return authenticationMapper.toLoginResult(userResponse, accessToken, refreshToken);
    }

    public LoginResult refresh(String oldRefreshToken) {
        RotateRefreshTokenResult result = refreshTokenService.rotate(oldRefreshToken);

        String newRefreshToken = result.rawRefreshToken();
        String accessToken = tokenService.generateAccessToken(result.user());

        UserResponse userResponse = userMapper.toDto(result.user());

        return authenticationMapper.toLoginResult(userResponse, accessToken, newRefreshToken);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User user = userService.getCurrentUser();

        boolean currentPasswordCorrect = passwordEncoder.matches(request.currentPassword(), user.getPassword());
        if (!currentPasswordCorrect) {
            throw new InvalidPasswordChangeException("Current password is incorrect");
        }

        boolean sameAsCurrent = passwordEncoder.matches(request.newPassword(), user.getPassword());
        if (sameAsCurrent) {
            throw new InvalidPasswordChangeException(
                "New password must be different from the current password"
            );
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        refreshTokenRepository.deleteAllByUser(user);
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        refreshTokenService.delete(rawRefreshToken);
    }

    @Transactional
    public void logoutAllByUser() {
        User user = userService.getCurrentUser();

        refreshTokenRepository.deleteAllByUser(user);
    }
}
