package com.d4rksoul.base.ecommerce.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Getter
public class ValidationException extends HandledException {
  private final Map<String, String> errors;

  public ValidationException(String message, Map<String, String> errors) {
        super(message, HttpStatus.BAD_REQUEST, "VALIDATION_ERROR");
        this.errors = errors;
  }
}
