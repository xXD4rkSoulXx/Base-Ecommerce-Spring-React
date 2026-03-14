package com.d4rksoul.base.ecommerce.exception;

import org.springframework.http.HttpStatus;

public class PaymentFailedException extends HandledException {
    public PaymentFailedException(String message) {
        super(message, HttpStatus.PAYMENT_REQUIRED, "PAYMENT_FAILED");
    }
}
