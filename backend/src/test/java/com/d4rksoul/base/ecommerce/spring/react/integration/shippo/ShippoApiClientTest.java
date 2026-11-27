package com.d4rksoul.base.ecommerce.spring.react.integration.shippo;

import com.d4rksoul.base.ecommerce.spring.react.exception.ShippoApiException;
import com.d4rksoul.base.ecommerce.spring.react.integration.shippo.dto.ShippoShipmentRequest;
import com.d4rksoul.base.ecommerce.spring.react.integration.shippo.dto.ShippoShipmentResponse;
import com.d4rksoul.base.ecommerce.spring.react.integration.shippo.dto.ShippoTransactionRequest;
import com.d4rksoul.base.ecommerce.spring.react.integration.shippo.dto.ShippoTransactionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ShippoApiClientTest {

    private static final String BASE_URL = "https://api.goshippo.com";

    private MockRestServiceServer server;
    private ShippoApiClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        server = MockRestServiceServer.bindTo(builder).build();
        client = new ShippoApiClient(builder.build());
    }

    @Test
    @DisplayName("shipment: returns the id and the rates of the response")
    void createShipmentReturnsRates() {
        server.expect(requestTo(BASE_URL + "/shipments/"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess("""
                {
                  "object_id": "shp_1",
                  "rates": [
                    {
                      "object_id": "rate_1",
                      "amount": "5.20",
                      "currency": "EUR",
                      "provider": "DHL",
                      "servicelevel": {"name": "Standard", "token": "dhl_std"},
                      "estimated_days": 3
                    }
                  ]
                }
                """, MediaType.APPLICATION_JSON));

        ShippoShipmentResponse response = client.createShipment(ShippoShipmentRequest.builder().build());

        assertThat(response.objectId()).isEqualTo("shp_1");
        assertThat(response.rates()).singleElement().satisfies(rate -> {
            assertThat(rate.objectId()).isEqualTo("rate_1");
            assertThat(rate.amount()).isEqualTo("5.20");
            assertThat(rate.provider()).isEqualTo("DHL");
            assertThat(rate.servicelevel().token()).isEqualTo("dhl_std");
            assertThat(rate.estimatedDays()).isEqualTo(3);
        });

        server.verify();
    }

    @Test
    @DisplayName("shipment: a response without rates becomes an empty list")
    void createShipmentWithoutRatesReturnsEmptyList() {
        server.expect(requestTo(BASE_URL + "/shipments/"))
            .andRespond(withSuccess("{\"object_id\": \"shp_1\"}", MediaType.APPLICATION_JSON));

        ShippoShipmentResponse response = client.createShipment(ShippoShipmentRequest.builder().build());

        assertThat(response.rates()).isEmpty();
    }

    @Test
    @DisplayName("transaction: returns the status and the tracking number")
    void createTransactionReturnsTrackingNumber() {
        server.expect(requestTo(BASE_URL + "/transactions/"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess("""
                {"object_id": "trx_1", "status": "SUCCESS", "tracking_number": "TRK123"}
                """, MediaType.APPLICATION_JSON));

        ShippoTransactionResponse response = client.createTransaction(
            ShippoTransactionRequest.builder().rate("rate_1").build()
        );

        assertThat(response.status()).isEqualTo("SUCCESS");
        assertThat(response.trackingNumber()).isEqualTo("TRK123");
    }

    @Test
    @DisplayName("a response rejected by Shippo becomes a ShippoApiException with the action and status")
    void rejectedRequestBecomesShippoApiException() {
        ShippoShipmentRequest request = ShippoShipmentRequest.builder().build();
        server.expect(requestTo(BASE_URL + "/shipments/")).andRespond(withBadRequest());

        assertThatThrownBy(() -> client.createShipment(request))
            .isInstanceOf(ShippoApiException.class)
            .hasMessageContaining("create shipment")
            .hasMessageContaining("400")
            .hasCauseInstanceOf(RestClientResponseException.class);
    }

    @Test
    @DisplayName("a network failure becomes a ShippoApiException")
    void networkFailureBecomesShippoApiException() {
        ShippoTransactionRequest request = ShippoTransactionRequest.builder()
            .rate("rate_1")
            .build();

        server.expect(requestTo(BASE_URL + "/transactions/"))
            .andRespond(_ -> {
                throw new IOException("connection reset");
            });

        assertThatThrownBy(() -> client.createTransaction(request))
            .isInstanceOf(ShippoApiException.class)
            .hasMessageContaining("create transaction")
            .hasCauseInstanceOf(ResourceAccessException.class);
    }
}
