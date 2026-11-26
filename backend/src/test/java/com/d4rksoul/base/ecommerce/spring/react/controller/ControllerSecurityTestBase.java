package com.d4rksoul.base.ecommerce.spring.react.controller;

import com.d4rksoul.base.ecommerce.spring.react.config.CorsConfig;
import com.d4rksoul.base.ecommerce.spring.react.config.CorsProperties;
import com.d4rksoul.base.ecommerce.spring.react.config.StorageProperties;
import com.d4rksoul.base.ecommerce.spring.react.integration.stripe.StripeApiClient;
import com.d4rksoul.base.ecommerce.spring.react.security.SecurityConfig;
import com.d4rksoul.base.ecommerce.spring.react.service.AuthenticationService;
import com.d4rksoul.base.ecommerce.spring.react.service.CartService;
import com.d4rksoul.base.ecommerce.spring.react.service.OrderReturnService;
import com.d4rksoul.base.ecommerce.spring.react.service.OrderService;
import com.d4rksoul.base.ecommerce.spring.react.service.PaymentService;
import com.d4rksoul.base.ecommerce.spring.react.service.ProductService;
import com.d4rksoul.base.ecommerce.spring.react.service.ReviewService;
import com.d4rksoul.base.ecommerce.spring.react.service.ShippingRateService;
import com.d4rksoul.base.ecommerce.spring.react.service.StripeWebhookService;
import com.d4rksoul.base.ecommerce.spring.react.service.UserService;
import com.d4rksoul.base.ecommerce.spring.react.service.WishlistService;
import com.d4rksoul.base.ecommerce.spring.react.service.admin.AdminAuditLogService;
import com.d4rksoul.base.ecommerce.spring.react.service.admin.AdminCustomerService;
import com.d4rksoul.base.ecommerce.spring.react.service.admin.AdminDashboardCommunityService;
import com.d4rksoul.base.ecommerce.spring.react.service.admin.AdminDashboardOperationsService;
import com.d4rksoul.base.ecommerce.spring.react.service.admin.AdminDashboardSalesService;
import com.d4rksoul.base.ecommerce.spring.react.service.admin.AdminDiscountCodeService;
import com.d4rksoul.base.ecommerce.spring.react.service.admin.AdminOrderService;
import com.d4rksoul.base.ecommerce.spring.react.service.admin.AdminProductService;
import com.d4rksoul.base.ecommerce.spring.react.service.admin.AdminProductSkuImageService;
import com.d4rksoul.base.ecommerce.spring.react.service.admin.AdminProductSkuService;
import com.d4rksoul.base.ecommerce.spring.react.service.admin.AdminReturnService;
import com.d4rksoul.base.ecommerce.spring.react.service.admin.AdminReviewService;
import com.d4rksoul.base.ecommerce.spring.react.service.admin.AdminStockService;
import com.d4rksoul.base.ecommerce.spring.react.service.admin.AdminTeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;

@WebMvcTest
@Import({SecurityConfig.class, CorsConfig.class})
@EnableConfigurationProperties({StorageProperties.class, CorsProperties.class})
@TestPropertySource(properties = {
    "CORS_ALLOWED_ORIGINS=http://localhost:5173",
    "STORAGE_PUBLIC_BASE_URL=http://localhost:8080/uploads"
})
abstract class ControllerSecurityTestBase {

    @Autowired
    protected MockMvc mockMvc;

    @MockitoBean
    protected JwtDecoder jwtDecoder;

    @MockitoBean
    private AuthenticationService authenticationService;
    @MockitoBean
    private CartService cartService;
    @MockitoBean
    private ShippingRateService shippingRateService;
    @MockitoBean
    private PaymentService paymentService;
    @MockitoBean
    private OrderService orderService;
    @MockitoBean
    private OrderReturnService orderReturnService;
    @MockitoBean
    private ProductService productService;
    @MockitoBean
    private ReviewService reviewService;
    @MockitoBean
    private StripeApiClient stripeApiClient;
    @MockitoBean
    private StripeWebhookService stripeWebhookService;
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private WishlistService wishlistService;
    @MockitoBean
    private AdminAuditLogService adminAuditLogService;
    @MockitoBean
    private AdminCustomerService adminCustomerService;
    @MockitoBean
    private AdminDashboardSalesService adminDashboardSalesService;
    @MockitoBean
    private AdminDashboardOperationsService adminDashboardOperationsService;
    @MockitoBean
    private AdminDashboardCommunityService adminDashboardCommunityService;
    @MockitoBean
    private AdminDiscountCodeService adminDiscountCodeService;
    @MockitoBean
    private AdminOrderService adminOrderService;
    @MockitoBean
    private AdminProductService adminProductService;
    @MockitoBean
    private AdminProductSkuService adminProductSkuService;
    @MockitoBean
    private AdminProductSkuImageService adminProductSkuImageService;
    @MockitoBean
    private AdminReturnService adminReturnService;
    @MockitoBean
    private AdminReviewService adminReviewService;
    @MockitoBean
    private AdminStockService adminStockService;
    @MockitoBean
    private AdminTeamService adminTeamService;

    protected String bearer(String... permissions) {
        String token = "token-" + String.join("-", permissions);

        Jwt jwt = Jwt.withTokenValue(token)
            .header("alg", "RS256")
            .subject("user@example.com")
            .claim("permissions", List.of(permissions))
            .build();

        when(jwtDecoder.decode(token)).thenReturn(jwt);

        return "Bearer " + token;
    }
}
