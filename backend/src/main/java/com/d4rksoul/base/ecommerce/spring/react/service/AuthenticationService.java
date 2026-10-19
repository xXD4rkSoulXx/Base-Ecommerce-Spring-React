package com.d4rksoul.base.ecommerce.spring.react.service;

import com.d4rksoul.base.ecommerce.spring.react.dto.result.LoginResult;
import com.d4rksoul.base.ecommerce.spring.react.dto.result.RotateRefreshTokenResult;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.LoginRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.RegisterRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.UserResponse;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final AuthenticationMapper authenticationMapper;
    private final UserMapper userMapper;
    private final TokenService tokenService;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("Email address is already registered");
        }

        User user = authenticationMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(UserRole.CUSTOMER);

        userRepository.save(user);
    }

    public LoginResult login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String accessToken = tokenService.generateAccessToken(user);
        String refreshToken = tokenService.generateRefreshToken();
        refreshTokenService.create(user, refreshToken);

        UserResponse userResponse = userMapper.toDto(user);

        return new LoginResult(userResponse, accessToken, refreshToken);
    }

    public LoginResult refresh(String oldRefreshToken) {
        RotateRefreshTokenResult result = refreshTokenService.rotate(oldRefreshToken);

        String newRefreshToken = result.newRefreshToken();
        String accessToken = tokenService.generateAccessToken(result.user());

        UserResponse userResponse = userMapper.toDto(result.user());

        return new LoginResult(userResponse, accessToken, newRefreshToken);
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        refreshTokenService.delete(rawRefreshToken);
    }

    @Transactional
    public void logoutAllByUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        refreshTokenService.deleteAllByUser(user);
    }
}
