package com.d4rksoul.base.ecommerce.spring.react.exception;

import com.d4rksoul.base.ecommerce.spring.react.dto.response.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationExceptions(MethodArgumentNotValidException exception) {
        FieldError firstError = exception.getBindingResult().getFieldErrors().getFirst();
        String errorMessage = firstError.getDefaultMessage();

        return buildResponse(HttpStatus.BAD_REQUEST, errorMessage != null ? errorMessage : "Validation error");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDeniedException(AccessDeniedException exception) {
        String message = exception.getMessage() != null
                ? exception.getMessage()
                : "You do not have permission to access this resource.";

        return buildResponse(HttpStatus.FORBIDDEN, message);
    }

    @ExceptionHandler(RefreshTokenException.class)
    public ResponseEntity<ApiError> handleRefreshTokenExceptions(RefreshTokenException exception) {
        String message = exception.getMessage() != null
                ? exception.getMessage()
                : "Session invalid or expired. Please log in again.";

        return buildResponse(HttpStatus.UNAUTHORIZED, message);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleEmailAlreadyExistsException(EmailAlreadyExistsException exception) {
        return buildResponse(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiError> handleInvalidCredentialsException(InvalidCredentialsException exception) {
        return buildResponse(HttpStatus.UNAUTHORIZED, exception.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiError> handleUserNotFoundException(UserNotFoundException exception) {
        return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ApiError> handleProductNotFoundException(ProductNotFoundException exception) {
        return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(ProductNotPurchasedException.class)
    public ResponseEntity<ApiError> handleProductNotPurchasedException(ProductNotPurchasedException exception) {
        return buildResponse(HttpStatus.FORBIDDEN, exception.getMessage());
    }

    @ExceptionHandler(ProductAlreadyReviewedException.class)
    public ResponseEntity<ApiError> handleProductAlreadyReviewedException(ProductAlreadyReviewedException exception) {
        return buildResponse(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(UserNotAuthenticatedException.class)
    public ResponseEntity<ApiError> handleUserNotAuthenticatedException(UserNotAuthenticatedException exception) {
        return buildResponse(HttpStatus.UNAUTHORIZED, exception.getMessage());
    }

    @ExceptionHandler(ProductSkuNotFoundException.class)
    public ResponseEntity<ApiError> handleProductSkuNotFoundException(ProductSkuNotFoundException exception) {
        return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(InvalidDiscountCodeException.class)
    public ResponseEntity<ApiError> handleInvalidDiscountCodeException(InvalidDiscountCodeException exception) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(UnpackableCartException.class)
    public ResponseEntity<ApiError> handleUnpackableCartException(UnpackableCartException exception) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(EmptyCartException.class)
    public ResponseEntity<ApiError> handleEmptyCartException(EmptyCartException exception) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(ShippoApiException.class)
    public ResponseEntity<ApiError> handleShippoApiException() {
        return buildResponse(
                HttpStatus.BAD_GATEWAY,
                "Shipping options are temporarily unavailable. Please try again in a moment."
        );
    }

    @ExceptionHandler(NoUnifiedCarrierException.class)
    public ResponseEntity<ApiError> handleNoUnifiedCarrierException(NoUnifiedCarrierException exception) {
        return buildResponse(HttpStatus.UNPROCESSABLE_CONTENT, exception.getMessage());
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ApiError> handleInsufficientStock(InsufficientStockException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(InvalidShippingRateException.class)
    public ResponseEntity<ApiError> handleInvalidShippingRate(InvalidShippingRateException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(StripeApiException.class)
    public ResponseEntity<ApiError> handleStripeApi(StripeApiException ex) {
        return buildResponse(HttpStatus.BAD_GATEWAY, ex.getMessage());
    }

    @ExceptionHandler(InvalidWebhookSignatureException.class)
    public ResponseEntity<ApiError> handleInvalidWebhookSignature(InvalidWebhookSignatureException exception) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(CartLimitException.class)
    public ResponseEntity<ApiError> handleCartLimitException(CartLimitException exception) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ApiError> handleOrderNotFoundException(OrderNotFoundException exception) {
        return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(InvalidReturnRequestException.class)
    public ResponseEntity<ApiError> handleInvalidReturnRequest(InvalidReturnRequestException exception) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(ReturnNotAllowedException.class)
    public ResponseEntity<ApiError> handleReturnNotAllowed(ReturnNotAllowedException exception) {
        return buildResponse(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneralException() {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred on the server.");
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
