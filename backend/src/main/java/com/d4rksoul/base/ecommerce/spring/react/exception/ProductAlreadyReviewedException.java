package com.d4rksoul.base.ecommerce.spring.react.exception;

public class ProductAlreadyReviewedException extends RuntimeException {

    public ProductAlreadyReviewedException(String message) {
        super(message);
    }
}
