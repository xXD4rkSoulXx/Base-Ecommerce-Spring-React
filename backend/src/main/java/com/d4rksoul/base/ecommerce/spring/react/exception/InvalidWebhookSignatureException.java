package com.d4rksoul.base.ecommerce.spring.react.exception;

public class InvalidWebhookSignatureException extends RuntimeException {

    public InvalidWebhookSignatureException(String message, Throwable cause) {
        super(message, cause);
    }
}
