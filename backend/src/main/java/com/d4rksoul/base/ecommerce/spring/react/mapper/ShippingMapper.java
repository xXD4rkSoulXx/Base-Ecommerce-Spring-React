package com.d4rksoul.base.ecommerce.spring.react.mapper;

import com.d4rksoul.base.ecommerce.spring.react.config.ShippingOriginProperties;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.CheckoutAddressRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.result.PackedParcelResult;
import com.d4rksoul.base.ecommerce.spring.react.dto.result.PackingItemResult;
import com.d4rksoul.base.ecommerce.spring.react.entity.Order;
import com.d4rksoul.base.ecommerce.spring.react.entity.ProductSku;
import com.d4rksoul.base.ecommerce.spring.react.integration.shippo.dto.ShippoAddress;
import com.d4rksoul.base.ecommerce.spring.react.integration.shippo.dto.ShippoParcel;
import com.d4rksoul.base.ecommerce.spring.react.integration.shippo.dto.ShippoShipmentRequest;
import com.d4rksoul.base.ecommerce.spring.react.integration.shippo.dto.ShippoTransactionRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ShippingMapper {

    public PackingItemResult toPackingItem(ProductSku sku) {
        return new PackingItemResult(sku.getId(), sku.getWeight(), sku.getLength(), sku.getWidth(), sku.getHeight());
    }

    public ShippoAddress toShippoAddress(ShippingOriginProperties origin) {
        return ShippoAddress.builder()
                .name(origin.getName())
                .street1(origin.getAddressLine1())
                .city(origin.getCity())
                .state(origin.getState())
                .zip(origin.getPostalCode())
                .country(origin.getCountry())
                .phone(origin.getPhone())
                .build();
    }

    public ShippoAddress toShippoAddress(CheckoutAddressRequest destination) {
        return ShippoAddress.builder()
                .name(destination.recipientName())
                .street1(destination.addressLine1())
                .street2(destination.addressLine2())
                .city(destination.city())
                .state(destination.state())
                .zip(destination.postalCode())
                .country(destination.country())
                .phone(destination.phone())
                .build();
    }

    public ShippoAddress toShippoAddress(Order order) {
        return ShippoAddress.builder()
                .name(order.getRecipientName())
                .street1(order.getAddressLine1())
                .street2(order.getAddressLine2())
                .city(order.getCity())
                .state(order.getState())
                .zip(order.getPostalCode())
                .country(order.getCountry())
                .phone(order.getPhone())
                .build();
    }

    public ShippoShipmentRequest toShipmentRequest(
            ShippoAddress addressFrom,
            ShippoAddress addressTo,
            PackedParcelResult parcel
    ) {
        return ShippoShipmentRequest.builder()
                .addressFrom(addressFrom)
                .addressTo(addressTo)
                .parcels(List.of(
                        ShippoParcel.builder()
                                .length(parcel.lengthCm())
                                .width(parcel.widthCm())
                                .height(parcel.heightCm())
                                .distanceUnit("cm")
                                .weight(parcel.weightKg())
                                .massUnit("kg")
                                .build()
                ))
                .async(false)
                .build();
    }


    public ShippoTransactionRequest toTransactionRequest(String rateObjectId) {
        return ShippoTransactionRequest.builder()
                .rate(rateObjectId)
                .async(false)
                .build();
    }
}
