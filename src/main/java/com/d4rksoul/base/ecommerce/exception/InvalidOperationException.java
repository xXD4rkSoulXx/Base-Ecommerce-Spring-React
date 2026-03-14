package com.d4rksoul.base.ecommerce.exception;

import org.springframework.http.HttpStatus;

public class InvalidOperationException extends HandledException {
    public InvalidOperationException(String message) {
        super(message, HttpStatus.CONFLICT, "INVALID_OPERATION");
    }
}
