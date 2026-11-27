package com.d4rksoul.base.ecommerce.spring.react.scheduler;

import com.d4rksoul.base.ecommerce.spring.react.constant.Constants;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class RequestActivityFilterTest {

    private final RequestActivityFilter filter = new RequestActivityFilter();

    @Test
    @DisplayName("isActive: is active right after startup")
    void isActiveRightAfterStartup() {
        assertThat(filter.isActive()).isTrue();
    }

    @Test
    @DisplayName("isActiveAt: is inactive once the window has passed without requests")
    void isInactiveOnceTheWindowHasPassedWithoutRequests() {
        Instant afterWindow = filter.lastActivity().plus(Constants.SCHEDULER_ACTIVE_WINDOW);

        assertThat(filter.isActiveAt(afterWindow)).isFalse();
    }

    @Test
    @DisplayName("isActiveAt: is still active just inside the window")
    void isStillActiveJustInsideTheWindow() {
        Instant insideWindow = filter.lastActivity().plus(Constants.SCHEDULER_ACTIVE_WINDOW)
            .minusSeconds(1);

        assertThat(filter.isActiveAt(insideWindow)).isTrue();
    }

    @Test
    @DisplayName("doFilter: actuator requests do not count as activity")
    void actuatorRequestsDoNotCountAsActivity() throws ServletException, IOException {
        Instant before = filter.lastActivity();

        filter.doFilter(
            new MockHttpServletRequest("GET", "/actuator/health/liveness"),
            new MockHttpServletResponse(),
            new MockFilterChain()
        );

        assertThat(filter.lastActivity()).isEqualTo(before);
    }
}
