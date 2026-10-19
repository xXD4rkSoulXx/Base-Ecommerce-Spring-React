package com.d4rksoul.base.ecommerce.spring.react.exception;

public class StripeApiException extends RuntimeException {

    public StripeApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
