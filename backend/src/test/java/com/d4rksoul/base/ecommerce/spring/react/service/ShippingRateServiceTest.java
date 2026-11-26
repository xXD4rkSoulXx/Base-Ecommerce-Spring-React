package com.d4rksoul.base.ecommerce.spring.react.service;

import com.d4rksoul.base.ecommerce.spring.react.config.ShippingOriginProperties;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.CheckoutAddressRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.ShippingRateResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.result.PackedParcelItemsResult;
import com.d4rksoul.base.ecommerce.spring.react.dto.result.PackedParcelResult;
import com.d4rksoul.base.ecommerce.spring.react.dto.result.PackingItemResult;
import com.d4rksoul.base.ecommerce.spring.react.entity.Cart;
import com.d4rksoul.base.ecommerce.spring.react.entity.CartItem;
import com.d4rksoul.base.ecommerce.spring.react.entity.ProductSku;
import com.d4rksoul.base.ecommerce.spring.react.entity.User;
import com.d4rksoul.base.ecommerce.spring.react.exception.EmptyCartException;
import com.d4rksoul.base.ecommerce.spring.react.exception.NoUnifiedCarrierException;
import com.d4rksoul.base.ecommerce.spring.react.integration.shippo.ShippoApiClient;
import com.d4rksoul.base.ecommerce.spring.react.integration.shippo.dto.ShippoParcel;
import com.d4rksoul.base.ecommerce.spring.react.integration.shippo.dto.ShippoRate;
import com.d4rksoul.base.ecommerce.spring.react.integration.shippo.dto.ShippoServiceLevel;
import com.d4rksoul.base.ecommerce.spring.react.integration.shippo.dto.ShippoShipmentRequest;
import com.d4rksoul.base.ecommerce.spring.react.integration.shippo.dto.ShippoShipmentResponse;
import com.d4rksoul.base.ecommerce.spring.react.mapper.ShippingMapper;
import com.d4rksoul.base.ecommerce.spring.react.repository.CartItemRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.CartRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShippingRateServiceTest {

    private static final long CART_ID = 2L;
    private static final double SMALL = 20;
    private static final double BIG = 45;

    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private UserService userService;
    @Mock
    private ParcelPackingService parcelPackingService;
    @Mock
    private ShippoApiClient shippoApiClient;
    @Mock
    private ShippingOriginProperties originProperties;
    @Spy
    private ShippingMapper shippingMapper = new ShippingMapper();

    @Captor
    private ArgumentCaptor<List<PackingItemResult>> packedItems;
    @Captor
    private ArgumentCaptor<ShippoShipmentRequest> shipmentRequest;

    @InjectMocks
    private ShippingRateService service;

    private final User user = User.builder()
        .id(1L)
        .build();

    private final Cart cart = Cart.builder()
        .id(CART_ID)
        .user(user)
        .build();

    private final CheckoutAddressRequest address = new CheckoutAddressRequest(
        "Ana Silva",
        "Rua das Flores 1",
        null,
        "Lisboa",
        null,
        "1000-001",
        "PT",
        "+351912345678"
    );

    @Test
    @DisplayName("rejects the request when the user has no cart")
    void rejectsMissingCart() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getShippingRates(address))
            .isInstanceOf(EmptyCartException.class);
    }

    @Test
    @DisplayName("rejects the request when the cart has no items")
    void rejectsEmptyCart() {
        givenCart();

        assertThatThrownBy(() -> service.getShippingRates(address))
            .isInstanceOf(EmptyCartException.class);
    }

    @Test
    @DisplayName("packs one entry per unit and converts the weight from grams to kilograms")
    void packsOneEntryPerUnitInKilograms() {
        givenCart(cartItem(10L, 500, 3));
        givenParcels(parcel(SMALL));
        givenShippoRates(SMALL, rate("CTT", "Standard", "5.00", 2));

        service.getShippingRates(address);

        verify(parcelPackingService).pack(packedItems.capture());

        assertThat(packedItems.getValue()).hasSize(3);
        assertThat(packedItems.getValue()).allSatisfy(item -> {
            assertThat(item.productSkuId()).isEqualTo(10L);
            assertThat(item.weightKg()).isEqualTo(0.5);
        });
    }

    @Test
    @DisplayName("asks Shippo for the parcel size, in cm and kg, and the destination address")
    void sendsParcelAndAddressToShippo() {
        givenCart(cartItem(10L, 500, 1));
        givenParcels(parcel(SMALL));

        when(shippoApiClient.createShipment(any(ShippoShipmentRequest.class)))
            .thenReturn(response(rate("CTT", "Standard", "5.00", 2)));

        service.getShippingRates(address);

        verify(shippoApiClient).createShipment(shipmentRequest.capture());

        ShippoShipmentRequest request = shipmentRequest.getValue();
        ShippoParcel sent = request.parcels().getFirst();

        assertThat(request.async()).isFalse();
        assertThat(request.parcels()).hasSize(1);
        assertThat(sent.length()).isEqualTo(20);
        assertThat(sent.width()).isEqualTo(15);
        assertThat(sent.height()).isEqualTo(10);
        assertThat(sent.weight()).isEqualTo(0.5);
        assertThat(sent.distanceUnit()).isEqualTo("cm");
        assertThat(sent.massUnit()).isEqualTo("kg");
        assertThat(request.addressTo().name()).isEqualTo("Ana Silva");
        assertThat(request.addressTo().street1()).isEqualTo("Rua das Flores 1");
        assertThat(request.addressTo().zip()).isEqualTo("1000-001");
        assertThat(request.addressTo().country()).isEqualTo("PT");
    }

    @Test
    @DisplayName("turns a Shippo rate into a store rate")
    void turnsShippoRateIntoStoreRate() {
        givenCart(cartItem(10L, 500, 1));
        givenParcels(parcel(SMALL));
        givenShippoRates(SMALL, rate("CTT", "Standard", "5.00", 2));

        List<ShippingRateResponse> rates = service.getShippingRates(address);

        assertThat(rates).hasSize(1);
        assertThat(rates.getFirst().rateId()).isEqualTo("CTT-Standard");
        assertThat(rates.getFirst().carrier()).isEqualTo("CTT");
        assertThat(rates.getFirst().service()).isEqualTo("Standard");
        assertThat(rates.getFirst().amount()).isEqualByComparingTo("5.00");
        assertThat(rates.getFirst().currency()).isEqualTo("EUR");
        assertThat(rates.getFirst().estimatedDays()).isEqualTo("2 days");
    }

    @ParameterizedTest(name = "estimated days {0} is shown as {1}")
    @CsvSource(value = {
        "1, 1 day",
        "5, 5 days",
        "0, N/A",
        "NULL, N/A"
    }, nullValues = "NULL")
    @DisplayName("formats the estimated delivery time")
    void formatsEstimatedDays(Integer estimatedDays, String expected) {
        givenCart(cartItem(10L, 500, 1));
        givenParcels(parcel(SMALL));
        givenShippoRates(SMALL, rate("CTT", "Standard", "5.00", estimatedDays));

        List<ShippingRateResponse> rates = service.getShippingRates(address);

        assertThat(rates.getFirst().estimatedDays()).isEqualTo(expected);
    }

    @Test
    @DisplayName("asks Shippo once for identical parcels and multiplies the price")
    void identicalParcelsAreSentOnceAndPriceIsMultiplied() {
        givenCart(cartItem(10L, 500, 2));
        givenParcels(parcel(SMALL), parcel(SMALL));
        givenShippoRates(SMALL, rate("CTT", "Standard", "5.00", 2));

        List<ShippingRateResponse> rates = service.getShippingRates(address);

        verify(shippoApiClient, times(1))
            .createShipment(any(ShippoShipmentRequest.class));

        assertThat(rates).hasSize(1);
        assertThat(rates.getFirst().amount()).isEqualByComparingTo("10.00");
    }

    @Test
    @DisplayName("keeps only carriers that serve every parcel, sums the prices and uses the longest delivery time")
    void keepsOnlyCarriersThatServeEveryParcel() {
        givenCart(
            cartItem(10L, 500, 1),
            cartItem(11L, 3000, 1)
        );
        givenParcels(parcel(SMALL), parcel(BIG));
        givenShippoRates(
            SMALL,
            rate("CTT", "Standard", "5.00", 2),
            rate("DHL", "Express", "9.00", 1)
        );
        givenShippoRates(BIG, rate("CTT", "Standard", "8.00", 4));

        List<ShippingRateResponse> rates = service.getShippingRates(address);

        assertThat(rates).hasSize(1);
        assertThat(rates.getFirst().rateId()).isEqualTo("CTT-Standard");
        assertThat(rates.getFirst().amount()).isEqualByComparingTo("13.00");
        assertThat(rates.getFirst().estimatedDays()).isEqualTo("4 days");
    }

    @Test
    @DisplayName("rejects the order when no single carrier serves every parcel")
    void rejectsWhenNoCarrierServesEveryParcel() {
        givenCart(
            cartItem(10L, 500, 1),
            cartItem(11L, 3000, 1)
        );
        givenParcels(parcel(SMALL), parcel(BIG));
        givenShippoRates(SMALL, rate("CTT", "Standard", "5.00", 2));
        givenShippoRates(BIG, rate("DHL", "Express", "9.00", 1));

        assertThatThrownBy(() -> service.getShippingRates(address))
            .isInstanceOf(NoUnifiedCarrierException.class);
    }

    @Test
    @DisplayName("rejects the order when Shippo returns no rates")
    void rejectsWhenShippoReturnsNoRates() {
        givenCart(cartItem(10L, 500, 1));
        givenParcels(parcel(SMALL));
        givenShippoRates(SMALL);

        assertThatThrownBy(() -> service.getShippingRates(address))
            .isInstanceOf(NoUnifiedCarrierException.class);
    }

    @Test
    @DisplayName("counts a carrier service only once per parcel when Shippo repeats it")
    void countsRepeatedServiceOncePerParcel() {
        givenCart(cartItem(10L, 500, 1));
        givenParcels(parcel(SMALL));
        givenShippoRates(
            SMALL,
            rate("CTT", "Standard", "5.00", 2),
            rate("CTT", "Standard", "7.00", 3)
        );

        List<ShippingRateResponse> rates = service.getShippingRates(address);

        assertThat(rates).hasSize(1);
        assertThat(rates.getFirst().amount()).isEqualByComparingTo("5.00");
    }

    private void givenCart(CartItem... items) {
        when(userService.getCurrentUser()).thenReturn(user);
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdOrderByIdAsc(CART_ID)).thenReturn(List.of(items));
    }

    private void givenParcels(PackedParcelResult... parcels) {
        List<PackedParcelItemsResult> packed = Arrays.stream(parcels)
            .map(parcel ->
                PackedParcelItemsResult.builder()
                    .parcel(parcel)
                    .items(List.of())
                    .build()
            )
            .toList();

        when(parcelPackingService.pack(anyList())).thenReturn(packed);
    }

    private void givenShippoRates(double parcelLengthCm, ShippoRate... rates) {
        when(shippoApiClient.createShipment(
            argThat(request ->
                request != null && request.parcels().getFirst().length() == parcelLengthCm
            )
        )).thenReturn(response(rates));
    }

    private static ShippoShipmentResponse response(ShippoRate... rates) {
        return new ShippoShipmentResponse("shp_1", List.of(rates));
    }

    private static ShippoRate rate(
        String provider, String service, String amount, Integer estimatedDays
    ) {
        return new ShippoRate(
            "rate_" + provider,
            amount,
            "EUR",
            provider,
            new ShippoServiceLevel(service, service.toLowerCase()),
            estimatedDays
        );
    }

    private static PackedParcelResult parcel(double lengthCm) {
        return PackedParcelResult.builder()
            .boxName("Box")
            .weightKg(0.5)
            .lengthCm(lengthCm)
            .widthCm(15)
            .heightCm(10)
            .build();
    }

    private static CartItem cartItem(long skuId, int weightGrams, int quantity) {
        ProductSku sku = ProductSku.builder()
            .id(skuId)
            .weightGrams(weightGrams)
            .lengthCm(10)
            .widthCm(10)
            .heightCm(5)
            .build();

        return CartItem.builder().productSku(sku).quantity(quantity).build();
    }
}
