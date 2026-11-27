package com.d4rksoul.base.ecommerce.spring.react.scheduler;

import com.d4rksoul.base.ecommerce.spring.react.constant.Constants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class RequestActivityFilter extends OncePerRequestFilter {

    private final AtomicReference<Instant> lastActivity = new AtomicReference<>(Instant.now());

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/actuator/");
    }

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        lastActivity.set(Instant.now());
        filterChain.doFilter(request, response);
    }

    public boolean isActive() {
        return isActiveAt(Instant.now());
    }

    boolean isActiveAt(Instant now) {
        return Duration.between(lastActivity.get(), now)
            .compareTo(Constants.SCHEDULER_ACTIVE_WINDOW) < 0;
    }

    Instant lastActivity() {
        return lastActivity.get();
    }
}
