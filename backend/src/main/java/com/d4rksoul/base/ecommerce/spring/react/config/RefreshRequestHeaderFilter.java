package com.d4rksoul.base.ecommerce.spring.react.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class RefreshRequestHeaderFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        boolean isRefreshRequest = "/api/auth/refresh".equals(request.getRequestURI())
                && "POST".equalsIgnoreCase(request.getMethod());

        if (isRefreshRequest && request.getHeader("X-Requested-With") == null) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"message\":\"Missing required request header.\"}");

            return;
        }

        filterChain.doFilter(request, response);
    }
}
