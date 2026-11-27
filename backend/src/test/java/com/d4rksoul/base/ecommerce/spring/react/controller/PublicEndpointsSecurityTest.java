package com.d4rksoul.base.ecommerce.spring.react.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.test.web.servlet.MvcResult;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PublicEndpointsSecurityTest extends ControllerSecurityTestBase {

    static Stream<Arguments> publicEndpoints() {
        return Stream.of(
            arguments(HttpMethod.GET, "/api/products"),
            arguments(HttpMethod.GET, "/api/products/1"),
            arguments(HttpMethod.GET, "/api/products/1/related"),
            arguments(HttpMethod.GET, "/api/products/1/reviews"),
            arguments(HttpMethod.GET, "/api/products/featured"),
            arguments(HttpMethod.GET, "/api/products/deals"),
            arguments(HttpMethod.GET, "/api/products/best-sellers"),
            arguments(HttpMethod.GET, "/api/products/top-rated"),
            arguments(HttpMethod.GET, "/api/products/search"),
            arguments(HttpMethod.GET, "/uploads/photo.jpg"),
            arguments(HttpMethod.GET, "/v3/api-docs"),
            arguments(HttpMethod.GET, "/swagger-ui/index.html"),
            arguments(HttpMethod.GET, "/actuator/health/liveness"),
            arguments(HttpMethod.POST, "/api/auth/register"),
            arguments(HttpMethod.POST, "/api/auth/login"),
            arguments(HttpMethod.POST, "/api/auth/refresh"),
            arguments(HttpMethod.DELETE, "/api/auth/logout"),
            arguments(HttpMethod.POST, "/api/webhooks/stripe")
        );
    }

    static Stream<Arguments> privateEndpoints() {
        return Stream.of(
            arguments(HttpMethod.GET, "/api/cart"),
            arguments(HttpMethod.POST, "/api/cart"),
            arguments(HttpMethod.POST, "/api/cart/merge"),
            arguments(HttpMethod.POST, "/api/cart/discount"),
            arguments(HttpMethod.PUT, "/api/cart/1"),
            arguments(HttpMethod.DELETE, "/api/cart/1"),
            arguments(HttpMethod.DELETE, "/api/cart/discount"),
            arguments(HttpMethod.DELETE, "/api/cart"),
            arguments(HttpMethod.POST, "/api/checkout/shipping-rates"),
            arguments(HttpMethod.POST, "/api/checkout/session"),
            arguments(HttpMethod.GET, "/api/orders"),
            arguments(HttpMethod.GET, "/api/orders/1"),
            arguments(HttpMethod.POST, "/api/orders/1/cancel"),
            arguments(HttpMethod.POST, "/api/orders/1/returns"),
            arguments(HttpMethod.GET, "/api/orders/1/returns/2/slip"),
            arguments(HttpMethod.GET, "/api/users/me"),
            arguments(HttpMethod.PATCH, "/api/users/me"),
            arguments(HttpMethod.POST, "/api/wishlist/1"),
            arguments(HttpMethod.POST, "/api/wishlist/merge"),
            arguments(HttpMethod.GET, "/api/wishlist"),
            arguments(HttpMethod.DELETE, "/api/wishlist/1"),
            arguments(HttpMethod.DELETE, "/api/wishlist"),
            arguments(HttpMethod.POST, "/api/products/1/review"),
            arguments(HttpMethod.GET, "/api/products/1/review/eligibility"),
            arguments(HttpMethod.PUT, "/api/auth/password"),
            arguments(HttpMethod.DELETE, "/api/auth/logout-all"),
            arguments(HttpMethod.GET, "/actuator/env")
        );
    }

    @ParameterizedTest(name = "{0} {1} is open without a token")
    @MethodSource("publicEndpoints")
    @DisplayName("public endpoints: are never blocked by authentication")
    void publicEndpointsDoNotRequireAuthentication(HttpMethod method, String path) throws Exception {
        MvcResult result = mockMvc.perform(
            request(method, path)
                .header("X-Requested-With", "XMLHttpRequest")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
                .with(csrf())
        ).andReturn();

        assertThat(isBlockedBySecurity(result)).isFalse();
    }

    @ParameterizedTest(name = "{0} {1} needs a session")
    @MethodSource("privateEndpoints")
    @DisplayName("private endpoints: answer 401 when there is no token")
    void privateEndpointsRequireAuthentication(HttpMethod method, String path) throws Exception {
        mockMvc.perform(
            request(method, path)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
                .with(csrf())
        ).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("private endpoints: answer 401 when the token is invalid or expired")
    void privateEndpointsRejectInvalidToken() throws Exception {
        when(jwtDecoder.decode("bad-token")).thenThrow(new BadJwtException("Invalid token"));

        mockMvc.perform(
            get("/api/cart").header(HttpHeaders.AUTHORIZATION, "Bearer bad-token")
        ).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("private endpoints: a customer token without any permission can use customer routes")
    void customerTokenReachesCustomerRoutes() throws Exception {
        MvcResult result = mockMvc.perform(
            get("/api/cart").header(HttpHeaders.AUTHORIZATION, bearer())
        ).andReturn();

        assertThat(isBlockedBySecurity(result)).isFalse();
    }

    private boolean isBlockedBySecurity(MvcResult result) {
        int status = result.getResponse().getStatus();
        boolean reachedController = result.getHandler() != null;

        return !reachedController && (status == 401 || status == 403);
    }
}
