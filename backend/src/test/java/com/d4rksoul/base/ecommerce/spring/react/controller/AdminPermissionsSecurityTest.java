package com.d4rksoul.base.ecommerce.spring.react.controller;

import com.d4rksoul.base.ecommerce.spring.react.enums.Permission;
import com.d4rksoul.base.ecommerce.spring.react.enums.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.PathPatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static com.d4rksoul.base.ecommerce.spring.react.enums.Permission.AUDIT_READ;
import static com.d4rksoul.base.ecommerce.spring.react.enums.Permission.CUSTOMERS_READ;
import static com.d4rksoul.base.ecommerce.spring.react.enums.Permission.DISCOUNTS_READ;
import static com.d4rksoul.base.ecommerce.spring.react.enums.Permission.DISCOUNTS_WRITE;
import static com.d4rksoul.base.ecommerce.spring.react.enums.Permission.ORDERS_READ;
import static com.d4rksoul.base.ecommerce.spring.react.enums.Permission.ORDERS_WRITE;
import static com.d4rksoul.base.ecommerce.spring.react.enums.Permission.PRODUCTS_READ;
import static com.d4rksoul.base.ecommerce.spring.react.enums.Permission.PRODUCTS_WRITE;
import static com.d4rksoul.base.ecommerce.spring.react.enums.Permission.RETURNS_READ;
import static com.d4rksoul.base.ecommerce.spring.react.enums.Permission.RETURNS_WRITE;
import static com.d4rksoul.base.ecommerce.spring.react.enums.Permission.REVIEWS_READ;
import static com.d4rksoul.base.ecommerce.spring.react.enums.Permission.REVIEWS_WRITE;
import static com.d4rksoul.base.ecommerce.spring.react.enums.Permission.STOCK_READ;
import static com.d4rksoul.base.ecommerce.spring.react.enums.Permission.STOCK_WRITE;
import static com.d4rksoul.base.ecommerce.spring.react.enums.Permission.TEAM_READ;
import static com.d4rksoul.base.ecommerce.spring.react.enums.Permission.TEAM_WRITE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminPermissionsSecurityTest extends ControllerSecurityTestBase {

    private record AdminRoute(
        HttpMethod method,

        String path,

        List<Permission> required
    ) {

        String key() {
            return method.name() + " " + path;
        }
    }

    private static AdminRoute route(
        HttpMethod method, String path, Permission... required
    ) {
        return new AdminRoute(method, path, List.of(required));
    }

    private static final List<AdminRoute> ROUTES = List.of(
        route(HttpMethod.GET, "/api/admin/audit-logs", AUDIT_READ),

        route(HttpMethod.GET, "/api/admin/customers", CUSTOMERS_READ),
        route(HttpMethod.GET, "/api/admin/customers/{id}", CUSTOMERS_READ),
        route(HttpMethod.GET, "/api/admin/customers/{id}/orders", CUSTOMERS_READ, ORDERS_READ),

        route(HttpMethod.GET, "/api/admin/dashboard/summary", ORDERS_READ),
        route(HttpMethod.GET, "/api/admin/dashboard/sales", ORDERS_READ),
        route(HttpMethod.GET, "/api/admin/dashboard/orders-by-status", ORDERS_READ),
        route(HttpMethod.GET, "/api/admin/dashboard/sales-by-category", ORDERS_READ),
        route(HttpMethod.GET, "/api/admin/dashboard/recent-orders", ORDERS_READ),
        route(HttpMethod.GET, "/api/admin/dashboard/top-products", PRODUCTS_READ),
        route(HttpMethod.GET, "/api/admin/dashboard/top-discount-codes", DISCOUNTS_READ),
        route(HttpMethod.GET, "/api/admin/dashboard/pending-orders", ORDERS_READ),
        route(HttpMethod.GET, "/api/admin/dashboard/pending-returns", RETURNS_READ),
        route(HttpMethod.GET, "/api/admin/dashboard/low-stock", STOCK_READ),
        route(HttpMethod.GET, "/api/admin/dashboard/returns-to-handle", RETURNS_READ),
        route(HttpMethod.GET, "/api/admin/dashboard/lowest-stock", STOCK_READ),
        route(HttpMethod.GET, "/api/admin/dashboard/customers", CUSTOMERS_READ),
        route(HttpMethod.GET, "/api/admin/dashboard/reviews", REVIEWS_READ),
        route(HttpMethod.GET, "/api/admin/dashboard/recent-activity", AUDIT_READ),

        route(HttpMethod.POST, "/api/admin/discount-codes", DISCOUNTS_WRITE),
        route(HttpMethod.POST, "/api/admin/discount-codes/{id}/activate", DISCOUNTS_WRITE),
        route(HttpMethod.POST, "/api/admin/discount-codes/{id}/deactivate", DISCOUNTS_WRITE),
        route(HttpMethod.GET, "/api/admin/discount-codes", DISCOUNTS_READ),
        route(HttpMethod.PUT, "/api/admin/discount-codes/{id}", DISCOUNTS_WRITE),

        route(HttpMethod.GET, "/api/admin/orders", ORDERS_READ),
        route(HttpMethod.GET, "/api/admin/orders/{id}", ORDERS_READ),
        route(HttpMethod.GET, "/api/admin/orders/{id}/shipping-slip", ORDERS_READ),
        route(HttpMethod.PUT, "/api/admin/orders/{id}/status", ORDERS_WRITE),
        route(HttpMethod.PUT, "/api/admin/orders/{orderId}/shipments/{shipmentId}/status", ORDERS_WRITE),

        route(HttpMethod.POST, "/api/admin/products", PRODUCTS_WRITE),
        route(HttpMethod.POST, "/api/admin/products/{id}/archive", PRODUCTS_WRITE),
        route(HttpMethod.POST, "/api/admin/products/{id}/restore", PRODUCTS_WRITE),
        route(HttpMethod.GET, "/api/admin/products", PRODUCTS_READ),
        route(HttpMethod.GET, "/api/admin/products/{id}", PRODUCTS_READ),
        route(HttpMethod.PUT, "/api/admin/products/{id}", PRODUCTS_WRITE),

        route(HttpMethod.POST, "/api/admin/products/{productId}/skus", PRODUCTS_WRITE),
        route(HttpMethod.POST, "/api/admin/products/{productId}/skus/{skuId}/archive", PRODUCTS_WRITE),
        route(HttpMethod.POST, "/api/admin/products/{productId}/skus/{skuId}/restore", PRODUCTS_WRITE),
        route(HttpMethod.GET, "/api/admin/products/{productId}/skus", PRODUCTS_READ),
        route(HttpMethod.PUT, "/api/admin/products/{productId}/skus/{skuId}", PRODUCTS_WRITE),

        route(HttpMethod.POST, "/api/admin/products/{productId}/skus/{skuId}/images", PRODUCTS_WRITE),
        route(HttpMethod.GET, "/api/admin/products/{productId}/skus/{skuId}/images", PRODUCTS_READ),
        route(
            HttpMethod.PUT,
            "/api/admin/products/{productId}/skus/{skuId}/images/{imageId}/product-cover",
            PRODUCTS_WRITE
        ),
        route(
            HttpMethod.PUT,
            "/api/admin/products/{productId}/skus/{skuId}/images/{imageId}/sku-cover",
            PRODUCTS_WRITE
        ),
        route(
            HttpMethod.DELETE,
            "/api/admin/products/{productId}/skus/{skuId}/images/{imageId}",
            PRODUCTS_WRITE
        ),

        route(HttpMethod.POST, "/api/admin/returns/{id}/approve", RETURNS_WRITE),
        route(HttpMethod.POST, "/api/admin/returns/{id}/reject", RETURNS_WRITE),
        route(HttpMethod.POST, "/api/admin/returns/{id}/receive", RETURNS_WRITE),
        route(HttpMethod.POST, "/api/admin/returns/{id}/refund", RETURNS_WRITE),
        route(HttpMethod.GET, "/api/admin/returns", RETURNS_READ),

        route(HttpMethod.POST, "/api/admin/reviews/{id}/hide", REVIEWS_WRITE),
        route(HttpMethod.POST, "/api/admin/reviews/{id}/show", REVIEWS_WRITE),
        route(HttpMethod.GET, "/api/admin/reviews", REVIEWS_READ),

        route(HttpMethod.POST, "/api/admin/stock/{skuId}/adjustments", STOCK_WRITE),
        route(HttpMethod.GET, "/api/admin/stock", STOCK_READ),
        route(HttpMethod.PUT, "/api/admin/stock/{skuId}", STOCK_WRITE),

        route(HttpMethod.GET, "/api/admin/team", TEAM_READ),
        route(HttpMethod.PUT, "/api/admin/team/role", TEAM_WRITE)
    );

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping handlerMapping;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    static Stream<Arguments> adminRoutes() {
        return ROUTES.stream().map(route ->
            arguments(route.method(), route.path(), route.required())
        );
    }

    static Stream<Arguments> roleExpectations() {
        return Stream.of(
            arguments(UserRole.CUSTOMER, HttpMethod.GET, "/api/admin/dashboard/summary", false),
            arguments(UserRole.SUPPORT, HttpMethod.GET, "/api/admin/orders", true),
            arguments(UserRole.SUPPORT, HttpMethod.POST, "/api/admin/returns/{id}/refund", true),
            arguments(UserRole.SUPPORT, HttpMethod.PUT, "/api/admin/stock/{skuId}", false),
            arguments(UserRole.SUPPORT, HttpMethod.GET, "/api/admin/audit-logs", false),
            arguments(UserRole.FULFILLMENT, HttpMethod.PUT, "/api/admin/orders/{id}/status", true),
            arguments(UserRole.FULFILLMENT, HttpMethod.PUT, "/api/admin/stock/{skuId}", true),
            arguments(UserRole.FULFILLMENT, HttpMethod.POST, "/api/admin/products", false),
            arguments(UserRole.MERCHANDISER, HttpMethod.POST, "/api/admin/products", true),
            arguments(UserRole.MERCHANDISER, HttpMethod.POST, "/api/admin/returns/{id}/refund", false),
            arguments(UserRole.MANAGER, HttpMethod.GET, "/api/admin/team", true),
            arguments(UserRole.MANAGER, HttpMethod.GET, "/api/admin/audit-logs", true),
            arguments(UserRole.MANAGER, HttpMethod.PUT, "/api/admin/team/role", false),
            arguments(UserRole.ADMIN, HttpMethod.PUT, "/api/admin/team/role", true)
        );
    }

    @Test
    @DisplayName("admin routes: the table covers every admin route of the application and nothing else")
    void tableCoversEveryAdminRoute() {
        Set<String> actualRoutes = new HashSet<>();

        for (
            Map.Entry<RequestMappingInfo, HandlerMethod> entry
                : handlerMapping.getHandlerMethods().entrySet()
        ) {
            RequestMappingInfo info = entry.getKey();

            for (RequestMethod method : info.getMethodsCondition().getMethods()) {
                for (String pattern : patternsOf(info)) {
                    if (pattern.startsWith("/api/admin")) {
                        actualRoutes.add(method.name() + " " + pattern);
                    }
                }
            }
        }

        List<String> expectedRoutes = ROUTES.stream().map(AdminRoute::key).toList();

        assertThat(actualRoutes).containsExactlyInAnyOrderElementsOf(expectedRoutes);
    }

    @ParameterizedTest(name = "{0} {1} is denied without any permission")
    @MethodSource("adminRoutes")
    @DisplayName("admin route: a user with no permission is denied")
    void deniedWithoutAnyPermission(HttpMethod method, String path) throws Exception {
        boolean denied = isAccessDenied(method, path, EnumSet.noneOf(Permission.class));

        assertThat(denied).isTrue();
    }

    @ParameterizedTest(name = "{0} {1} is denied when a required permission is missing")
    @MethodSource("adminRoutes")
    @DisplayName("admin route: a user with every permission except a required one is denied")
    void deniedWhenARequiredPermissionIsMissing(
        HttpMethod method, String path, List<Permission> required
    ) throws Exception {
        for (Permission missing : required) {
            boolean denied = isAccessDenied(method, path, EnumSet.complementOf(EnumSet.of(missing)));

            assertThat(denied)
                .as("%s %s must be denied without %s", method, path, missing)
                .isTrue();
        }
    }

    @ParameterizedTest(name = "{0} {1} is allowed with the required permissions")
    @MethodSource("adminRoutes")
    @DisplayName("admin route: a user with exactly the required permissions is allowed")
    void allowedWithTheRequiredPermissions(
        HttpMethod method, String path, List<Permission> required
    ) throws Exception {
        boolean denied = isAccessDenied(method, path, EnumSet.copyOf(required));

        assertThat(denied).isFalse();
    }

    @ParameterizedTest(name = "{0} on {1} {2} -> allowed: {3}")
    @MethodSource("roleExpectations")
    @DisplayName("roles: each role can only reach the admin routes its permissions allow")
    void rolesReachOnlyTheirRoutes(
        UserRole role, HttpMethod method, String path, boolean allowed
    ) throws Exception {
        boolean denied = isAccessDenied(method, path, role.getPermissions());

        assertThat(denied).isEqualTo(!allowed);
    }

    @Test
    @DisplayName("http: an admin route answers 401 without a token")
    void adminRouteAnswers401WithoutToken() throws Exception {
        mockMvc.perform(get("/api/admin/team"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("http: an admin route answers 403 to a token without the permission")
    void adminRouteAnswers403WithoutPermission() throws Exception {
        mockMvc.perform(get("/api/admin/team").header(HttpHeaders.AUTHORIZATION, bearer()))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("http: an admin route is reachable with a token that carries the permission")
    void adminRouteIsReachableWithPermission() throws Exception {
        MvcResult result = mockMvc.perform(
            get("/api/admin/team")
                .header(HttpHeaders.AUTHORIZATION, bearer("TEAM_READ"))
        ).andReturn();

        assertThat(result.getResponse().getStatus()).isNotIn(401, 403);
    }

    private boolean isAccessDenied(
        HttpMethod method, String path, Set<Permission> permissions
    ) throws IllegalAccessException {
        HandlerMethod handler = findHandler(method, path).createWithResolvedBean();
        String[] authorities = permissions.stream().map(Enum::name).toArray(String[]::new);
        SecurityContextHolder.getContext()
            .setAuthentication(
                new TestingAuthenticationToken("user", "password", authorities)
            );

        Method controllerMethod = handler.getMethod();
        try {
            controllerMethod.invoke(handler.getBean(), defaultArguments(controllerMethod));

            return false;
        } catch (InvocationTargetException exception) {
            return exception.getCause() instanceof AccessDeniedException;
        }
    }

    private HandlerMethod findHandler(HttpMethod method, String path) {
        for (
            Map.Entry<RequestMappingInfo, HandlerMethod> entry
                : handlerMapping.getHandlerMethods().entrySet()
        ) {
            RequestMappingInfo info = entry.getKey();
            boolean sameMethod = info.getMethodsCondition().getMethods().stream()
                .anyMatch(requestMethod -> requestMethod.name().equals(method.name()));
            boolean samePath = patternsOf(info).contains(path);

            if (sameMethod && samePath) {
                return entry.getValue();
            }
        }

        throw new IllegalStateException("No handler found for " + method + " " + path);
    }

    private Object[] defaultArguments(Method method) {
        Class<?>[] types = method.getParameterTypes();
        Object[] arguments = new Object[types.length];

        for (int i = 0; i < types.length; i++) {
            arguments[i] = types[i].isPrimitive()
                ? Array.get(Array.newInstance(types[i], 1), 0)
                : null;
        }

        return arguments;
    }

    private Set<String> patternsOf(RequestMappingInfo info) {
        PathPatternsRequestCondition condition = info.getPathPatternsCondition();

        return condition == null ? Set.of() : condition.getPatternValues();
    }
}
