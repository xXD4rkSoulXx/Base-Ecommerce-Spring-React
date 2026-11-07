package com.d4rksoul.base.ecommerce.spring.react.exception;

public class ProductSkuAlreadyExistsException extends RuntimeException {

    public ProductSkuAlreadyExistsException(String message) {
        super(message);
    }
}