package com.d4rksoul.base.ecommerce.spring.react.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message) {
        super(message);
    }
}
