package com.d4rksoul.base.ecommerce.spring.react.config;

import com.d4rksoul.base.ecommerce.spring.react.constant.Constants;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;

@Configuration
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "cloudinary")
public class CloudinaryConfig {

    @Bean
    public RestClient cloudinaryRestClient(CloudinaryProperties properties) {
        HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Constants.CLOUDINARY_CONNECT_TIMEOUT)
            .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Constants.CLOUDINARY_READ_TIMEOUT);

        return RestClient.builder()
            .requestFactory(requestFactory)
            .baseUrl(properties.baseUrl() + "/" + properties.cloudName())
            .build();
    }
}
