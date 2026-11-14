package com.d4rksoul.base.ecommerce.spring.react.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.nio.file.AccessDeniedException;

@RequiredArgsConstructor
public class RefreshRequestHeaderFilter extends OncePerRequestFilter {

    private final HandlerExceptionResolver resolver;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        boolean isRefreshRequest = "/api/auth/refresh".equals(request.getServletPath())
            && "POST".equalsIgnoreCase(request.getMethod());

        if (isRefreshRequest && !"XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            resolver.resolveException(
                request, response, null, new AccessDeniedException("Missing required request header.")
            );

            return;
        }

        filterChain.doFilter(request, response);
    }
}
