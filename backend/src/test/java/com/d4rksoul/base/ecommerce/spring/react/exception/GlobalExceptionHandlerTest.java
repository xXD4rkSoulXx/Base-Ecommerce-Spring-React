package com.d4rksoul.base.ecommerce.spring.react.exception;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private static final RuntimeException CAUSE = new RuntimeException("cause");

    private ThrowingController controller;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        controller = new ThrowingController();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    static Stream<Arguments> mappedExceptions() {
        return Stream.of(
            arguments(new InvalidDiscountCodeException("m"), 400, "m"),
            arguments(new UnpackableCartException("m"), 400, "m"),
            arguments(new EmptyCartException("m"), 400, "m"),
            arguments(new InvalidShippingRateException("m"), 400, "m"),
            arguments(new InvalidWebhookSignatureException("m", CAUSE), 400, "m"),
            arguments(new CartLimitException("m"), 400, "m"),
            arguments(new InvalidReturnRequestException("m"), 400, "m"),
            arguments(new InvalidPasswordChangeException("m"), 400, "m"),
            arguments(new InvalidTeamChangeException("m"), 400, "m"),
            arguments(new InvalidImageException("m"), 400, "m"),
            arguments(new InvalidProductSkuException("m"), 400, "m"),
            arguments(new InvalidStockAdjustmentException("m"), 400, "m"),

            arguments(new InvalidCredentialsException("m"), 401, "m"),
            arguments(new UserNotAuthenticatedException("m"), 401, "m"),
            arguments(new InvalidRefreshTokenException("m"), 401, "m"),
            arguments(new RefreshTokenExpiredException("m"), 401, "m"),
            arguments(new RefreshTokenReuseException("m"), 401, "m"),

            arguments(new ProductNotPurchasedException("m"), 403, "m"),
            arguments(new AccessDeniedException("m"), 403, "m"),

            arguments(new UserNotFoundException("m"), 404, "m"),
            arguments(new ProductNotFoundException("m"), 404, "m"),
            arguments(new ProductSkuNotFoundException("m"), 404, "m"),
            arguments(new ProductSkuImageNotFoundException("m"), 404, "m"),
            arguments(new OrderNotFoundException("m"), 404, "m"),
            arguments(new ReturnNotFoundException("m"), 404, "m"),
            arguments(new DiscountCodeNotFoundException("m"), 404, "m"),
            arguments(new ReviewNotFoundException("m"), 404, "m"),

            arguments(new EmailAlreadyExistsException("m"), 409, "m"),
            arguments(new ProductAlreadyReviewedException("m"), 409, "m"),
            arguments(new InsufficientStockException("m"), 409, "m"),
            arguments(new ReturnNotAllowedException("m"), 409, "m"),
            arguments(new InvalidOrderTransitionException("m"), 409, "m"),
            arguments(new ProductRefAlreadyExistsException("m"), 409, "m"),
            arguments(new FeaturedSlotTakenException("m"), 409, "m"),
            arguments(new ProductSkuAlreadyExistsException("m"), 409, "m"),
            arguments(new ProductUnavailableException("m"), 409, "m"),
            arguments(new DiscountCodeAlreadyExistsException("m"), 409, "m"),
            arguments(new CheckoutInProgressException("m"), 409, "m"),
            arguments(
                new DataIntegrityViolationException("could not execute statement; constraint [uk_internal]"),
                409,
                "This action conflicts with existing data. Please refresh and try again."
            ),

            arguments(new MaxUploadSizeExceededException(5_242_880L), 413, "The image must be at most 5 MB."),

            arguments(new NoUnifiedCarrierException("m"), 422, "m"),

            arguments(new StripeApiException("m", CAUSE), 502, "m"),
            arguments(new RefundFailedException("m"), 502, "m"),
            arguments(
                new ShippoApiException("internal shippo detail", CAUSE),
                502,
                "Shipping options are temporarily unavailable. Please try again in a moment."
            ),
            arguments(
                new ImageStorageException("internal cloudinary detail"),
                502,
                "Image storage is temporarily unavailable. Please try again in a moment."
            ),

            arguments(
                new IllegalStateException("internal detail that must not leak"),
                500,
                "An unexpected error occurred on the server."
            )
        );
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @MethodSource("mappedExceptions")
    @DisplayName("each exception is mapped to its status and ApiError body")
    void mapsExceptionToStatusAndBody(
        Exception exception, int expectedStatus, String expectedMessage
    ) throws Exception {
        controller.exception = exception;

        mockMvc.perform(get("/test/throw"))
            .andExpect(status().is(expectedStatus))
            .andExpect(jsonPath("$.status").value(expectedStatus))
            .andExpect(
                jsonPath("$.error")
                    .value(HttpStatus.valueOf(expectedStatus).getReasonPhrase())
            )
            .andExpect(jsonPath("$.message").value(expectedMessage))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("access denied without a message falls back to the default text")
    void accessDeniedWithoutMessageUsesDefault() throws Exception {
        controller.exception = new AccessDeniedException("ignored") {
            @Override
            public String getMessage() {
                return null;
            }
        };

        mockMvc.perform(get("/test/throw"))
            .andExpect(status().isForbidden())
            .andExpect(
                jsonPath("$.message")
                    .value("You do not have permission to access this resource.")
            );
    }

    @Test
    @DisplayName("a refresh token exception without a message falls back to the default text")
    void refreshTokenWithoutMessageUsesDefault() throws Exception {
        controller.exception = new InvalidRefreshTokenException(null);

        mockMvc.perform(get("/test/throw"))
            .andExpect(status().isUnauthorized())
            .andExpect(
                jsonPath("$.message")
                    .value("Session invalid or expired. Please log in again.")
            );
    }

    @Test
    @DisplayName("a malformed JSON body answers 400 without leaking parser details")
    void malformedBodyAnswers400() throws Exception {
        mockMvc.perform(
            post("/test/body")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{not json")
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Malformed request body"));
    }

    @Test
    @DisplayName("a bean validation failure answers 400 with the first field message")
    void validationFailureAnswers400() throws Exception {
        mockMvc.perform(
            post("/test/body")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Name is required"));
    }

    @Test
    @DisplayName("a missing request parameter answers 400")
    void missingParameterAnswers400() throws Exception {
        mockMvc.perform(get("/test/param"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Invalid request parameter"));
    }

    @Test
    @DisplayName("a parameter of the wrong type answers 400")
    void wrongParameterTypeAnswers400() throws Exception {
        mockMvc.perform(get("/test/param").param("n", "abc"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Invalid request parameter"));
    }

    @Test
    @DisplayName("an unsupported content type answers 415")
    void unsupportedContentTypeAnswers415() throws Exception {
        mockMvc.perform(post("/test/body").contentType(MediaType.TEXT_PLAIN).content("name"))
            .andExpect(status().isUnsupportedMediaType())
            .andExpect(jsonPath("$.message").value("Unsupported content type"));
    }

    @Test
    @DisplayName("an unsupported HTTP method answers 405")
    void unsupportedMethodAnswers405() throws Exception {
        mockMvc.perform(post("/test/throw"))
            .andExpect(status().isMethodNotAllowed())
            .andExpect(jsonPath("$.message").value("Method not allowed"));
    }

    private record Payload(
        @NotBlank(message = "Name is required") String name
    ) {}

    @RestController
    private static class ThrowingController {

        private Exception exception;

        @GetMapping("/test/throw")
        String throwConfigured() throws Exception {
            throw exception;
        }

        @PostMapping("/test/body")
        String body(@Valid @RequestBody Payload payload) {
            return payload.name();
        }

        @GetMapping("/test/param")
        String param(@RequestParam int n) {
            return String.valueOf(n);
        }
    }
}

