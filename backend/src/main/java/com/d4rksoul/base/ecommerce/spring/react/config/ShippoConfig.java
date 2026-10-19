package com.d4rksoul.base.ecommerce.spring.react.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ShippoConfig {

    @Value("${SHIPPO_API_KEY}")
    private String apiKey;

    @Value("${SHIPPO_BASE_URL}")
    private String baseUrl;

    @Bean
    public RestClient shippoRestClient() {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeaders(headers -> headers.set("Authorization", "ShippoToken " + apiKey))
                .build();
    }
}
