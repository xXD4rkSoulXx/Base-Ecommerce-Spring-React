package com.d4rksoul.base.ecommerce.spring.react.storage;

import com.d4rksoul.base.ecommerce.spring.react.config.StorageProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.time.Duration;

@Configuration
public class LocalStorageResourceConfig implements WebMvcConfigurer {

    private final String location;

    public LocalStorageResourceConfig(StorageProperties properties) {
        String uri = Path.of(properties.uploadDir()).toAbsolutePath().normalize().toUri().toString();

        this.location = uri.endsWith("/") ? uri : uri + "/";
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location)
                .setCacheControl(CacheControl.maxAge(Duration.ofDays(30)).immutable());
    }
}
