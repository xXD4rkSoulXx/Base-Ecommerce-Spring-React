package com.d4rksoul.base.ecommerce.spring.react.storage;

import com.d4rksoul.base.ecommerce.spring.react.config.CloudinaryProperties;
import com.d4rksoul.base.ecommerce.spring.react.exception.ImageStorageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class CloudinaryImageStorageTest {

    private static final String BASE_URL = "https://api.cloudinary.com/v1_1";
    private static final String API_URL = BASE_URL + "/demo";
    private static final String SECURE_URL =
        "https://res.cloudinary.com/demo/image/upload/v1/velo-citi/photo.jpg";
    private static final String API_SECRET = "api-secret-value";

    private MockRestServiceServer server;
    private CloudinaryImageStorage storage;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(API_URL);
        server = MockRestServiceServer.bindTo(builder).build();
        storage = new CloudinaryImageStorage(
            builder.build(),
            new CloudinaryProperties(
                BASE_URL,
                "demo",
                "key",
                API_SECRET,
                "velo-citi"
            )
        );
    }

    @Test
    @DisplayName("Signs parameters in alphabetical order with SHA-256")
    void signsParametersAlphabetically() {
        String signature = CloudinaryImageStorage.sign(
            Map.of(
                "timestamp", "1315060510",
                "public_id", "sample_image",
                "eager", "w_400,h_300,c_pad|w_260,h_200,c_crop"
            ),
            "abcd"
        );

        assertThat(signature)
            .isEqualTo("cc927e1290f9e3ae4c1a741eda21a4630b4ce80f9ce0bc0296337d25cf40f91e");
    }

    @Test
    @DisplayName("Uploads a signed image and returns the secure URL")
    void storeReturnsSecureUrl() {
        server.expect(requestTo(API_URL + "/image/upload"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(request -> {
                String body = ((MockClientHttpRequest) request).getBodyAsString();

                assertThat(body).contains("name=\"api_key\"", "name=\"signature\"", "name=\"timestamp\"");
                assertThat(body).doesNotContain(API_SECRET);
            })
            .andRespond(
                withSuccess("{\"secure_url\":\"" + SECURE_URL + "\"}", MediaType.APPLICATION_JSON)
            );

        String url = storage.store(new byte[]{1, 2, 3}, "jpg");

        assertThat(url).isEqualTo(SECURE_URL);

        server.verify();
    }

    @Test
    @DisplayName("Fails when Cloudinary rejects the upload")
    void storeFailsWhenCloudinaryRejects() {
        server.expect(requestTo(API_URL + "/image/upload")).andRespond(withServerError());

        assertThatThrownBy(() -> storage.store(new byte[]{1}, "jpg"))
            .isInstanceOf(ImageStorageException.class);
    }

    @Test
    @DisplayName("Fails when the response has no secure URL")
    void storeFailsWithoutSecureUrl() {
        server.expect(requestTo(API_URL + "/image/upload"))
            .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> storage.store(new byte[]{1}, "jpg"))
            .isInstanceOf(ImageStorageException.class);
    }

    @Test
    @DisplayName("Deletes by the public id extracted from the stored URL")
    void deleteUsesPublicIdFromUrl() {
        server.expect(requestTo(API_URL + "/image/destroy"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(request ->
                    assertThat(((MockClientHttpRequest) request).getBodyAsString()
                )
                .contains("public_id=velo-citi%2Fabc")
            )
            .andRespond(withSuccess("{\"result\":\"ok\"}", MediaType.APPLICATION_JSON));

        storage.delete("https://res.cloudinary.com/demo/image/upload/v1700000000/velo-citi/abc.jpg");

        server.verify();
    }

    @Test
    @DisplayName("Ignores URLs that are not from this Cloudinary account")
    void deleteIgnoresForeignUrl() {
        storage.delete("http://localhost:8080/uploads/photo.jpg");
        storage.delete("https://res.cloudinary.com/other/image/upload/v1/velo-citi/abc.jpg");

        server.verify();
    }

    @Test
    @DisplayName("Fails when Cloudinary rejects the delete")
    void deleteFailsWhenCloudinaryRejects() {
        server.expect(requestTo(API_URL + "/image/destroy")).andRespond(withServerError());

        assertThatThrownBy(() -> storage.delete(
            "https://res.cloudinary.com/demo/image/upload/v1/velo-citi/abc.jpg"
        )).isInstanceOf(ImageStorageException.class);
    }
}
