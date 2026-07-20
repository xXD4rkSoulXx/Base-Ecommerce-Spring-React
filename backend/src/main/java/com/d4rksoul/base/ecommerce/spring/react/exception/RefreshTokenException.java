package com.d4rksoul.base.ecommerce.spring.react.exception;

public abstract class RefreshTokenException extends RuntimeException {

    protected RefreshTokenException(String message) {
        super(message);
    }
}
