package com.d4rksoul.base.ecommerce.security.filter;

import com.d4rksoul.base.ecommerce.dto.api.ApiError;
import com.d4rksoul.base.ecommerce.dto.api.AuthResponse;
import com.d4rksoul.base.ecommerce.dto.request.SignInDto;
import com.d4rksoul.base.ecommerce.security.jwt.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        setFilterProcessesUrl("/api/login");
    }

    @Override
    public @NonNull Authentication attemptAuthentication(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse respons
    ) throws AuthenticationServiceException {
        try {
            SignInDto authRequest = new ObjectMapper().readValue(request.getInputStream(), SignInDto.class);

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    authRequest.email(), authRequest.password());

            return authenticationManager.authenticate(authToken);
        } catch (IOException e) {
            throw new AuthenticationServiceException("Erro ao ler dados de login", e);
        }
    }

    @Override
    protected void successfulAuthentication(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain chain,
            @NonNull Authentication authResult
    ) throws IOException {
        String token = null;
        Object principal = authResult.getPrincipal();

        if (principal instanceof UserDetails userDetails) {
            token = jwtUtil.generateToken(userDetails);
        }

        AuthResponse authResponse = AuthResponse.builder()
                .success(true)
                .message("Login Successful")
                .token(token)
                .role(authResult.getAuthorities().iterator().next().getAuthority())
                .expirationTime(jwtUtil.getExpirationTime())
                .build();

        response.addHeader("Authorization", "Bearer " + token);

        response.setContentType("application/json");

        new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
    }

    @Override
    protected void unsuccessfulAuthentication(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull AuthenticationException failed
    ) throws IOException {
        ApiError apiError = ApiError.builder()
                .status(HttpServletResponse.SC_UNAUTHORIZED)
                .error("Unauthorized")
                .message("Login failed: " + failed.getMessage())
                .errorCode("AUTH_001")
                .path(request.getRequestURI())
                .build();

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        response.setContentType("application/json");

        new ObjectMapper().writeValue(response.getOutputStream(), apiError);
    }
}
