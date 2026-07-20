package com.d4rksoul.base.ecommerce.spring.react.security;

public interface HashService {

    String hash(String input);
    boolean matches(String rawInput, String hashedValue);
}
