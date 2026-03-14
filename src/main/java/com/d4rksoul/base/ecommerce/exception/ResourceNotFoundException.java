package com.d4rksoul.base.ecommerce.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends HandledException {
    public ResourceNotFoundException(String resource) {
        super(resource + " not found", HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND");
    }
}
