package com.d4rksoul.base.ecommerce.spring.react.exception;

import com.d4rksoul.base.ecommerce.spring.react.dto.response.ApiError;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
        InvalidDiscountCodeException.class,
        UnpackableCartException.class,
        EmptyCartException.class,
        InvalidShippingRateException.class,
        InvalidWebhookSignatureException.class,
        CartLimitException.class,
        InvalidReturnRequestException.class,
        InvalidPasswordChangeException.class,
        InvalidTeamChangeException.class,
        InvalidImageException.class,
        InvalidProductSkuException.class,
        InvalidStockAdjustmentException.class
    })
    public ResponseEntity<ApiError> handleBadRequest(RuntimeException exception) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationExceptions(MethodArgumentNotValidException exception) {
        FieldError firstError = exception.getBindingResult().getFieldErrors().getFirst();
        String errorMessage = firstError.getDefaultMessage();

        return buildResponse(
            HttpStatus.BAD_REQUEST, errorMessage != null ? errorMessage : "Validation error"
        );
    }

    @ExceptionHandler({
        MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class
    })
    public ResponseEntity<ApiError> handleInvalidParameter() {
        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid request parameter");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadableMessage() {
        return buildResponse(HttpStatus.BAD_REQUEST, "Malformed request body");
    }

    @ExceptionHandler({
        EmailAlreadyExistsException.class,
        ProductAlreadyReviewedException.class,
        InsufficientStockException.class,
        ReturnNotAllowedException.class,
        InvalidOrderTransitionException.class,
        ProductRefAlreadyExistsException.class,
        FeaturedSlotTakenException.class,
        ProductSkuAlreadyExistsException.class,
        ProductUnavailableException.class,
        DiscountCodeAlreadyExistsException.class,
        CheckoutInProgressException.class
    })
    public ResponseEntity<ApiError> handleConflict(RuntimeException exception) {
        return buildResponse(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolationException() {
        return buildResponse(
            HttpStatus.CONFLICT,
            "This action conflicts with existing data. Please refresh and try again."
        );
    }

    @ExceptionHandler({InvalidCredentialsException.class, UserNotAuthenticatedException.class})
    public ResponseEntity<ApiError> handleUnauthorized(RuntimeException exception) {
        return buildResponse(HttpStatus.UNAUTHORIZED, exception.getMessage());
    }

    @ExceptionHandler(RefreshTokenException.class)
    public ResponseEntity<ApiError> handleRefreshTokenExceptions(RefreshTokenException exception) {
        String message = exception.getMessage() != null
            ? exception.getMessage()
            : "Session invalid or expired. Please log in again.";

        return buildResponse(HttpStatus.UNAUTHORIZED, message);
    }

    @ExceptionHandler(ProductNotPurchasedException.class)
    public ResponseEntity<ApiError> handleProductNotPurchased(ProductNotPurchasedException exception) {
        return buildResponse(HttpStatus.FORBIDDEN, exception.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDeniedException(AccessDeniedException exception) {
        String message = exception.getMessage() != null
            ? exception.getMessage()
            : "You do not have permission to access this resource.";

        return buildResponse(HttpStatus.FORBIDDEN, message);
    }

    @ExceptionHandler({
        UserNotFoundException.class,
        ProductNotFoundException.class,
        ProductSkuNotFoundException.class,
        ProductSkuImageNotFoundException.class,
        OrderNotFoundException.class,
        ReturnNotFoundException.class,
        DiscountCodeNotFoundException.class,
        ReviewNotFoundException.class
    })
    public ResponseEntity<ApiError> handleNotFound(RuntimeException exception) {
        return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleNoResourceFound() {
        return buildResponse(HttpStatus.NOT_FOUND, "Resource not found");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethodNotSupported() {
        return buildResponse(HttpStatus.METHOD_NOT_ALLOWED, "Method not allowed");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiError> handleMaxUploadSizeExceeded() {
        return buildResponse(HttpStatus.CONTENT_TOO_LARGE, "The image must be at most 5 MB.");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiError> handleMediaTypeNotSupported() {
        return buildResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported content type");
    }

    @ExceptionHandler(NoUnifiedCarrierException.class)
    public ResponseEntity<ApiError> handleNoUnifiedCarrier(NoUnifiedCarrierException exception) {
        return buildResponse(HttpStatus.UNPROCESSABLE_CONTENT, exception.getMessage());
    }

    @ExceptionHandler({StripeApiException.class, RefundFailedException.class})
    public ResponseEntity<ApiError> handleBadGateway(RuntimeException exception) {
        return buildResponse(HttpStatus.BAD_GATEWAY, exception.getMessage());
    }

    @ExceptionHandler(ShippoApiException.class)
    public ResponseEntity<ApiError> handleShippoApiException() {
        return buildResponse(
            HttpStatus.BAD_GATEWAY,
            "Shipping options are temporarily unavailable. Please try again in a moment."
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneralException() {
        return buildResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred on the server."
        );
    }

    private ResponseEntity<ApiError> buildResponse(HttpStatus status, String message) {
        ApiError error = ApiError.builder()
            .status(status.value())
            .error(status.getReasonPhrase())
            .message(message)
            .timestamp(Instant.now())
            .build();

        return ResponseEntity.status(status).body(error);
    }
}
