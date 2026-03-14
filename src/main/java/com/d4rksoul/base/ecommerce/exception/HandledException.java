package com.d4rksoul.base.ecommerce.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class HandledException extends RuntimeException {
    private final HttpStatus status;
    private final String errorCode;

    protected HandledException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }
}
