package com.d4rksoul.base.ecommerce.spring.react.integration.shippo;

import com.d4rksoul.base.ecommerce.spring.react.exception.ShippoApiException;
import com.d4rksoul.base.ecommerce.spring.react.integration.shippo.dto.ShippoShipmentRequest;
import com.d4rksoul.base.ecommerce.spring.react.integration.shippo.dto.ShippoShipmentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@RequiredArgsConstructor
public class ShippoApiClient {

    private final RestClient shippoRestClient;

    public ShippoShipmentResponse createShipment(ShippoShipmentRequest request) {
        try {
            return shippoRestClient.post()
                    .uri("/shipments/")
                    .body(request)
                    .retrieve()
                    .body(ShippoShipmentResponse.class);
        } catch (RestClientException e) {
            throw new ShippoApiException("Failed to create shipment with Shippo.", e);
        }
    }
}
