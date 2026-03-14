package com.d4rksoul.base.ecommerce.exception;

import org.springframework.http.HttpStatus;

public class BusinessRuleException extends HandledException {
    public BusinessRuleException(String message) {
        super(message, HttpStatus.valueOf(422), "BUSINESS_RULE_VIOLATION");
    }
}
