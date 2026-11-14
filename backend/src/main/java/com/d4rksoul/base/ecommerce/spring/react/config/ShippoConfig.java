package com.d4rksoul.base.ecommerce.spring.react.config;

import com.d4rksoul.base.ecommerce.spring.react.constant.Constants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;

@Configuration
public class ShippoConfig {

    @Bean
    public RestClient shippoRestClient(ShippoProperties properties) {
        HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Constants.SHIPPO_CONNECT_TIMEOUT)
            .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Constants.SHIPPO_READ_TIMEOUT);

        return RestClient.builder()
            .requestFactory(requestFactory)
            .baseUrl(properties.baseUrl())
            .defaultHeaders(headers ->
                headers.set("Authorization", "ShippoToken " + properties.apiKey())
            )
            .build();
    }
}
