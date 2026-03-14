package com.d4rksoul.base.ecommerce.exception;

import org.springframework.http.HttpStatus;

public class InsufficientBalanceException extends HandledException {
    public InsufficientBalanceException(String message) {
        super(message, HttpStatus.CONFLICT, "INSUFFICIENT_BALANCE");
    }
}
