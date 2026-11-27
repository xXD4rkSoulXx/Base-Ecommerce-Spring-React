package com.d4rksoul.base.ecommerce.spring.react.storage;

import com.d4rksoul.base.ecommerce.spring.react.config.BackendProperties;
import com.d4rksoul.base.ecommerce.spring.react.config.StorageProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "local", matchIfMissing = true)
public class LocalImageStorage implements ImageStorage {

    private final Path root;
    private final String urlPrefix;

    public LocalImageStorage(
        StorageProperties properties, BackendProperties backendProperties
    ) {
        this.root = Path.of(properties.uploadDir()).toAbsolutePath().normalize();
        this.urlPrefix = backendProperties.url() + "/uploads/";
    }

    @Override
    public String store(byte[] content, String extension) {
        String filename = UUID.randomUUID() + "." + extension;

        try {
            Files.createDirectories(root);
            Files.write(root.resolve(filename), content);
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }

        return urlPrefix + filename;
    }

    @Override
    public void delete(String url) {
        if (!url.startsWith(urlPrefix)) {
            return;
        }

        Path file = root.resolve(url.substring(urlPrefix.length())).normalize();

        if (!file.startsWith(root)) {
            return;
        }

        try {
            Files.deleteIfExists(file);
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}
