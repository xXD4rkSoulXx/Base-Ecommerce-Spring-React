package com.d4rksoul.base.ecommerce.spring.react.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Configuration
public class LocalStorageResourceConfig implements WebMvcConfigurer {

    private final String location;

    public LocalStorageResourceConfig(@Value("${app.storage.upload-dir}") String uploadDir) {
        String uri = Path.of(uploadDir).toAbsolutePath().normalize().toUri().toString();

        this.location = uri.endsWith("/") ? uri : uri + "/";
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location)
                .setCacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).immutable());
    }
}
