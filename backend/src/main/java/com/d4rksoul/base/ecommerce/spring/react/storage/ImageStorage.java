package com.d4rksoul.base.ecommerce.spring.react.storage;

public interface ImageStorage {

    String store(byte[] content, String extension);

    void delete(String url);
}
