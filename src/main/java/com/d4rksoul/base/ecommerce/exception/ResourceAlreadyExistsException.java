package com.d4rksoul.base.ecommerce.exception;

import org.springframework.http.HttpStatus;

public class ResourceAlreadyExistsException extends HandledException {
    public ResourceAlreadyExistsException(String resource) {
        super(resource + " already exists", HttpStatus.CONFLICT, "RESOURCE_ALREADY_EXISTS");
    }
}
