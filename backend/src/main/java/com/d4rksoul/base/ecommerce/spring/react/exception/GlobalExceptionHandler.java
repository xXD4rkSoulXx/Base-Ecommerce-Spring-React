package com.d4rksoul.base.ecommerce.spring.react.exception;

import com.d4rksoul.base.ecommerce.spring.react.dto.response.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;
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
