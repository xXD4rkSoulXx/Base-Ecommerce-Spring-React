package com.d4rksoul.base.ecommerce.spring.react.exception;

public class UserNotAuthenticatedException extends RuntimeException {

    public UserNotAuthenticatedException(String message) {
        super(message);
    }
}
