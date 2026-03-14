package com.d4rksoul.base.ecommerce.exception;

import org.springframework.http.HttpStatus;

public class PaymentNotAllowedException extends HandledException {
    public PaymentNotAllowedException(String message) {
        super(message, HttpStatus.FORBIDDEN, "PAYMENT_NOT_ALLOWED");
    }
}
