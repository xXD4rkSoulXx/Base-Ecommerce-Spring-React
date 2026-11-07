package com.d4rksoul.base.ecommerce.spring.react.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Component
public class LocalImageStorage implements ImageStorage {

    private final Path root;
    private final String urlPrefix;

    public LocalImageStorage(
            @Value("${app.storage.upload-dir}") String uploadDir,
            @Value("${app.storage.public-base-url}") String publicBaseUrl
    ) {
        this.root = Path.of(uploadDir).toAbsolutePath().normalize();
        this.urlPrefix = publicBaseUrl + "/uploads/";
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
