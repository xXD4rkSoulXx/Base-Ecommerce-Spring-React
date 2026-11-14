package com.d4rksoul.base.ecommerce.spring.react.integration.shippo;

import com.d4rksoul.base.ecommerce.spring.react.exception.ShippoApiException;
import com.d4rksoul.base.ecommerce.spring.react.integration.shippo.dto.ShippoShipmentRequest;
import com.d4rksoul.base.ecommerce.spring.react.integration.shippo.dto.ShippoShipmentResponse;
import com.d4rksoul.base.ecommerce.spring.react.integration.shippo.dto.ShippoTransactionRequest;
import com.d4rksoul.base.ecommerce.spring.react.integration.shippo.dto.ShippoTransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
@RequiredArgsConstructor
public class ShippoApiClient {

    private final RestClient shippoRestClient;

    public ShippoShipmentResponse createShipment(ShippoShipmentRequest request) {
        return post("/shipments/", request, ShippoShipmentResponse.class, "create shipment");
    }

    public ShippoTransactionResponse createTransaction(ShippoTransactionRequest request) {
        return post("/transactions/", request, ShippoTransactionResponse.class, "create transaction");
    }

    private <T> T post(String uri, Object request, Class<T> responseType, String action) {
        try {
            return shippoRestClient.post()
                .uri(uri)
                .body(request)
                .retrieve()
                .body(responseType);
        } catch (RestClientResponseException e) {
            throw new ShippoApiException(
                "Shippo rejected the request to " + action + " (" + e.getStatusCode() + "). ", e
            );
        } catch (RestClientException e) {
            throw new ShippoApiException("Failed to " + action + " with Shippo. ", e);
        }
    }
}
