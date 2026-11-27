package com.d4rksoul.base.ecommerce.spring.react.storage;

import com.d4rksoul.base.ecommerce.spring.react.config.CloudinaryProperties;
import com.d4rksoul.base.ecommerce.spring.react.exception.ImageStorageException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "cloudinary")
public class CloudinaryImageStorage implements ImageStorage {

    private final RestClient restClient;
    private final CloudinaryProperties properties;
    private final Pattern publicIdPattern;

    public CloudinaryImageStorage(
        RestClient cloudinaryRestClient, CloudinaryProperties properties
    ) {
        this.restClient = cloudinaryRestClient;
        this.properties = properties;
        this.publicIdPattern = Pattern.compile(
            "^https://res\\.cloudinary\\.com/" + Pattern.quote(properties.cloudName())
                + "/image/upload/(?:v\\d+/)?(.+)\\.[^./]+$"
        );
    }

    @Override
    public String store(byte[] content, String extension) {
        String publicId = properties.folder() + "/" + UUID.randomUUID();
        String timestamp = timestamp();

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(content) {
            @Override
            public String getFilename() {
                return publicId + "." + extension;
            }
        });
        body.add("api_key", properties.apiKey());
        body.add("public_id", publicId);
        body.add("timestamp", timestamp);
        body.add("signature", sign(
            Map.of("public_id", publicId, "timestamp", timestamp),
            properties.apiSecret()
        ));

        CloudinaryUploadResponse response;

        try {
            response = restClient.post()
                .uri("/image/upload")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .body(CloudinaryUploadResponse.class);
        } catch (RestClientException e) {
            throw new ImageStorageException("Failed to upload the image to Cloudinary. ", e);
        }

        if (response == null || response.secureUrl() == null) {
            throw new ImageStorageException("Cloudinary did not return an image URL.");
        }

        return response.secureUrl();
    }

    @Override
    public void delete(String url) {
        Matcher matcher = publicIdPattern.matcher(url);

        if (!matcher.matches()) {
            return;
        }

        String publicId = matcher.group(1);
        String timestamp = timestamp();

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("api_key", properties.apiKey());
        form.add("public_id", publicId);
        form.add("timestamp", timestamp);
        form.add("signature", sign(
            Map.of("public_id", publicId, "timestamp", timestamp),
            properties.apiSecret())
        );

        try {
            restClient.post()
                .uri("/image/destroy")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .toBodilessEntity();
        } catch (RestClientException e) {
            throw new ImageStorageException("Failed to delete the image from Cloudinary. ", e);
        }
    }

    static String sign(Map<String, String> parameters, String apiSecret) {
        String toSign = new TreeMap<>(parameters).entrySet().stream()
            .map(entry -> entry.getKey() + "=" + entry.getValue())
            .collect(Collectors.joining("&")) + apiSecret;

        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(toSign.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private String timestamp() {
        return String.valueOf(Instant.now().getEpochSecond());
    }
}
