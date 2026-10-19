package com.d4rksoul.base.ecommerce.spring.react.exception;

public class CartLimitException extends RuntimeException {

    public CartLimitException(String message) {
        super(message);
    }
}
