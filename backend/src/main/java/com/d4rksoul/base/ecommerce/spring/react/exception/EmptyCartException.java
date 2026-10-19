package com.d4rksoul.base.ecommerce.spring.react.exception;

public class EmptyCartException extends RuntimeException {

    public EmptyCartException(String message) {
        super(message);
    }
}
