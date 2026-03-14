package com.d4rksoul.base.ecommerce.exception;

import org.springframework.http.HttpStatus;

public class InvalidRequestException extends HandledException {
    public InvalidRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "INVALID_REQUEST");
    }
}
