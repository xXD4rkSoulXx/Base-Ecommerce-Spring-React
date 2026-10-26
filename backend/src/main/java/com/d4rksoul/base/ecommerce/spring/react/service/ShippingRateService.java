package com.d4rksoul.base.ecommerce.spring.react.service;

import com.d4rksoul.base.ecommerce.spring.react.config.ShippingOriginProperties;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.CheckoutAddressRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.ShippingRateResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.result.PackedParcelItemsResult;
import com.d4rksoul.base.ecommerce.spring.react.entity.Cart;
import com.d4rksoul.base.ecommerce.spring.react.entity.CartItem;
import com.d4rksoul.base.ecommerce.spring.react.entity.User;
import com.d4rksoul.base.ecommerce.spring.react.exception.EmptyCartException;
import com.d4rksoul.base.ecommerce.spring.react.exception.NoUnifiedCarrierException;
import com.d4rksoul.base.ecommerce.spring.react.exception.UserNotAuthenticatedException;
import com.d4rksoul.base.ecommerce.spring.react.integration.shippo.ShippoApiClient;
import com.d4rksoul.base.ecommerce.spring.react.integration.shippo.dto.ShippoAddress;
import com.d4rksoul.base.ecommerce.spring.react.integration.shippo.dto.ShippoRate;
import com.d4rksoul.base.ecommerce.spring.react.integration.shippo.dto.ShippoShipmentRequest;
import com.d4rksoul.base.ecommerce.spring.react.integration.shippo.dto.ShippoShipmentResponse;
import com.d4rksoul.base.ecommerce.spring.react.mapper.ShippingMapper;
import com.d4rksoul.base.ecommerce.spring.react.repository.CartItemRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.CartRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.UserRepository;
import com.d4rksoul.base.ecommerce.spring.react.dto.result.PackedParcelResult;
import com.d4rksoul.base.ecommerce.spring.react.dto.result.PackingItemResult;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShippingRateService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ParcelPackingService parcelPackingService;
    private final ShippingMapper shippingMapper;
    private final ShippoApiClient shippoApiClient;
    private final ShippingOriginProperties originProperties;

    @Transactional(readOnly = true)
    public List<ShippingRateResponse> getShippingRates(CheckoutAddressRequest destination) {
        User currentUser = getCurrentUser();
        Cart cart = cartRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new EmptyCartException("Your cart is empty."));

        List<CartItem> cartItems = cartItemRepository.findByCartIdOrderByIdAsc(cart.getId());

        if (cartItems.isEmpty()) {
            throw new EmptyCartException("Your cart is empty.");
        }

        List<PackingItemResult> packingItems = new ArrayList<>();

        for (CartItem cartItem : cartItems) {
            PackingItemResult packingItem = shippingMapper.toPackingItem(cartItem.getProductSku());

            for (int i = 0; i < cartItem.getQuantity(); i++) {
                packingItems.add(packingItem);
            }
        }

        List<PackedParcelResult> parcels = parcelPackingService.pack(packingItems).stream()
                .map(PackedParcelItemsResult::parcel)
                .toList();

        ShippoAddress addressFrom = shippingMapper.toShippoAddress(originProperties);
        ShippoAddress addressTo = shippingMapper.toShippoAddress(destination);

        Map<PackedParcelResult, Long> parcelGroups = parcels.stream()
                .collect(Collectors.groupingBy(Function.identity(), LinkedHashMap::new, Collectors.counting()));

        Map<String, RateAccumulator> aggregated = new LinkedHashMap<>();

        for (Map.Entry<PackedParcelResult, Long> parcelGroup : parcelGroups.entrySet()) {
            ShippoShipmentRequest shipmentRequest = shippingMapper.toShipmentRequest(
                    addressFrom, addressTo, parcelGroup.getKey()
            );
            ShippoShipmentResponse response = shippoApiClient.createShipment(shipmentRequest);

            List<ShippoRate> rates = response.rates() != null ? response.rates() : List.of();
            Set<String> seenInParcel = new HashSet<>();

            for (ShippoRate rate : rates) {
                String key = rate.provider() + "|" + rate.servicelevel().name();

                if (!seenInParcel.add(key)) {
                    continue;
                }

                aggregated.merge(
                        key,
                        new RateAccumulator(rate, parcelGroup.getValue().intValue()),
                        RateAccumulator::combine
                );
            }
        }

        int totalParcels = parcels.size();

        List<ShippingRateResponse> unifiedRates = aggregated.values().stream()
                .filter(accumulator -> accumulator.parcelCount == totalParcels)
                .map(RateAccumulator::toResponse)
                .toList();

        if (unifiedRates.isEmpty()) {
            throw new NoUnifiedCarrierException(
                    "No single carrier can fulfill this order to the given address. " +
                            "Some items may be too large or heavy to ship together."
            );
        }

        return unifiedRates;
    }

    private static class RateAccumulator {
        private final String carrier;
        private final String service;
        private final String currency;
        private BigDecimal amount;
        private int deliveryDays;
        private int parcelCount;

        RateAccumulator(ShippoRate rate, int parcelCount) {
            this.carrier = rate.provider();
            this.service = rate.servicelevel().name();
            this.currency = rate.currency();
            this.amount = new BigDecimal(rate.amount()).multiply(BigDecimal.valueOf(parcelCount));
            this.deliveryDays = rate.estimatedDays() != null ? rate.estimatedDays() : 0;
            this.parcelCount = parcelCount;
        }

        RateAccumulator combine(RateAccumulator other) {
            this.amount = this.amount.add(other.amount);
            this.deliveryDays = Math.max(this.deliveryDays, other.deliveryDays);
            this.parcelCount += other.parcelCount;

            return this;
        }

        ShippingRateResponse toResponse() {
            return ShippingRateResponse.builder()
                    .rateId(carrier + "-" + service)
                    .carrier(carrier)
                    .service(service)
                    .amount(amount)
                    .currency(currency)
                    .estimatedDays(formatDeliveryDays())
                    .build();
        }

        private String formatDeliveryDays() {
            if (deliveryDays <= 0) {
                return "N/A";
            }

            return deliveryDays == 1 ? "1 day" : deliveryDays + " days";
        }
    }

    private User getCurrentUser() {
        Jwt jwt = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(auth -> (Jwt) auth.getPrincipal())
                .orElseThrow(() -> new UserNotAuthenticatedException("User is not authenticated"));

        return userRepository.findByEmail(jwt.getSubject())
                .orElseThrow(() -> new UserNotAuthenticatedException("User is not authenticated"));
    }
}
