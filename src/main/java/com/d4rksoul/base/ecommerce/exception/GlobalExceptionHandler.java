/**
 *  A pasta exception contém todos os erros personalizados, de forma a evitar que o
 * website crashe em caso de erro feito pelo utilizador, e invês disso, ele lança só uma
 * mensagem de erro a informar qual erro ocorreu.
 *
 *
 *  O GlobalExceptionHandler contém todos os tipos de exceptions que não precisam ser
 * configurados manualmente, pois já existe import para isso, como por exemplo,
 * AuthenticationException.class.
 *
 *  Cada método comunica com a ApiError, API criada na pasta api para conter todos os
 * detalhes do erro em JSON e comunicar com o servidor.
 *
 *  O handleAllExceptionn é a Exception para apresentar erro desconhecido, que não é
 * nenhum dos definidos.
 *
 *  O handleHandledException é a Exception que vai buscar todas as exceptions que criou-se
 * manualmente, através de heranças.
 */


package com.d4rksoul.base.ecommerce.exception;

import com.d4rksoul.base.ecommerce.dto.response.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.security.sasl.AuthenticationException;
import java.nio.file.AccessDeniedException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAllExceptions(Exception exception, HttpServletRequest request) {
        log.error("Unhandled exception at {} {}",
                request.getMethod(),
                request.getRequestURI(),
                exception);

        ApiError error = ApiError.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("Unexpected error occurred. Please contact support.")
                .errorCode("INTERNAL_ERROR")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValid(MethodArgumentNotValidException exception, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));

        ApiError error = ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message("Validation failed")
                .errorCode("VALIDATION_ERROR")
                .path(request.getRequestURI())
                .errors(errors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException exception, HttpServletRequest request) {
        Map<String, String> errors = exception.getConstraintViolations()
                .stream()
                .collect(Collectors.toMap(v -> v.getPropertyPath().toString(), ConstraintViolation::getMessage));

        ApiError error = ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Error")
                .message("Invalid parameters")
                .errorCode("VALIDATION_ERROR")
                .path(request.getRequestURI())
                .errors(errors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleInvalidJson(HttpMessageNotReadableException _exception, HttpServletRequest request) {
        ApiError error = ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message("Malformed JSON request")
                .errorCode("INVALID_JSON")
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethodNotSupported(HttpRequestMethodNotSupportedException exception, HttpServletRequest request) {
        String supportedMethods = Optional.ofNullable(exception.getSupportedHttpMethods())
                .map(methods -> methods.stream()
                        .map(HttpMethod::name)
                        .collect(Collectors.joining(", ")))
                .orElse("");

        ApiError error = ApiError.builder()
                .status(HttpStatus.METHOD_NOT_ALLOWED.value())
                .error("Method Not Allowed")
                .message("HTTP method not allowed. Supported methods: " + supportedMethods)
                .errorCode("METHOD_NOT_ALLOWED")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException _exception, HttpServletRequest request) {
        ApiError error = ApiError.builder()
                .status(HttpStatus.FORBIDDEN.value())
                .error("Forbidden")
                .message("Access denied")
                .errorCode("ACCESS_DENIED")
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthentication(AuthenticationException _exception, HttpServletRequest request) {
        ApiError error = ApiError.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Unauthorized")
                .message("Authentication required")
                .errorCode("AUTH_ERROR")
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiError> handleNoHandlerFound(NoHandlerFoundException _exception, HttpServletRequest request) {
        ApiError error = ApiError.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message("The requested endpoint does not exist")
                .errorCode("NOT_FOUND")
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(HttpMediaTypeException.class)
    public ResponseEntity<ApiError> handleMediaTypeNotSupported(HttpMediaTypeException exception, HttpServletRequest request) {
        String supportedTypes = exception.getSupportedMediaTypes()
                .stream()
                .map(MediaType::toString)
                .collect(Collectors.joining(", "));

        ApiError error = ApiError.builder()
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value())
                .error("Unsupported Media Type")
                .message("Content-Type not supported. Supported types: " + supportedTypes)
                .errorCode("UNSUPPORTED_MEDIA_TYPE")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(error);
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ApiError> handleMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException exception, HttpServletRequest request) {
        String supportedTypes = Optional.of(exception.getSupportedMediaTypes())
                .orElse(Collections.emptyList())
                .stream()
                .map(MediaType::toString)
                .collect(Collectors.joining(", "));

        ApiError error = ApiError.builder()
                .status(HttpStatus.NOT_ACCEPTABLE.value())
                .error("Not Acceptable")
                .message("Accept header not supported. Supported types: " + supportedTypes)
                .errorCode("NOT_ACCEPTABLE")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(error);
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ApiError> handleFileUpload(MultipartException exception, HttpServletRequest request) {
        ApiError error = ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("File Upload Error")
                .message("Failed to upload file: " + exception.getMessage())
                .errorCode("FILE_UPLOAD_ERROR")
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(HandledException.class)
    public ResponseEntity<ApiError> handleHandledException(HandledException exception, HttpServletRequest request) {
        ApiError error = ApiError.builder()
                .status(exception.getStatus().value())
                .error(exception.getStatus().getReasonPhrase())
                .message(exception.getMessage())
                .errorCode(exception.getErrorCode())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(exception.getStatus()).body(error);
    }
}
