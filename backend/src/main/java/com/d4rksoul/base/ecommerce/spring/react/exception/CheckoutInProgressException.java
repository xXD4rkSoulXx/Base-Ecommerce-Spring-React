package com.d4rksoul.base.ecommerce.spring.react.exception;

public class CheckoutInProgressException extends RuntimeException {

    public CheckoutInProgressException(String message) {
        super(message);
    }
}
