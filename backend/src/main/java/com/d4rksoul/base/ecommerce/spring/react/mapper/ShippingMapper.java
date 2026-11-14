package com.d4rksoul.base.ecommerce.spring.react.mapper;

import com.d4rksoul.base.ecommerce.spring.react.config.ShippingOriginProperties;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.CheckoutAddressRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.admin.AdminCreateProductSkuRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.admin.AdminUpdateProductSkuRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.result.PackedParcelResult;
import com.d4rksoul.base.ecommerce.spring.react.dto.result.PackingItemResult;
import com.d4rksoul.base.ecommerce.spring.react.entity.Order;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderItem;
import com.d4rksoul.base.ecommerce.spring.react.entity.ProductSku;
import com.d4rksoul.base.ecommerce.spring.react.integration.shippo.dto.ShippoAddress;
import com.d4rksoul.base.ecommerce.spring.react.integration.shippo.dto.ShippoParcel;
import com.d4rksoul.base.ecommerce.spring.react.integration.shippo.dto.ShippoShipmentRequest;
import com.d4rksoul.base.ecommerce.spring.react.integration.shippo.dto.ShippoTransactionRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ShippingMapper {

    public ShippoAddress toShippoAddress(ShippingOriginProperties origin) {
        return ShippoAddress.builder()
            .name(origin.name())
            .street1(origin.addressLine1())
            .city(origin.city())
            .state(origin.state())
            .zip(origin.postalCode())
            .country(origin.country())
            .phone(origin.phone())
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

    public PackingItemResult toPackingItem(ProductSku sku) {
        return PackingItemResult.builder()
            .productSkuId(sku.getId())
            .weightKg(sku.getWeightGrams() / 1000.0)
            .lengthCm(sku.getLengthCm())
            .widthCm(sku.getWidthCm())
            .heightCm(sku.getHeightCm())
            .build();
    }

    public PackingItemResult toPackingItem(OrderItem item) {
        return PackingItemResult.builder()
            .productSkuId(item.getProductSku().getId())
            .weightKg(item.getWeightGrams() / 1000.0)
            .lengthCm(item.getLengthCm())
            .widthCm(item.getWidthCm())
            .heightCm(item.getHeightCm())
            .build();
    }

    public PackingItemResult toPackingItem(AdminCreateProductSkuRequest request) {
        return PackingItemResult.builder()
            .weightKg(request.weightGrams() / 1000.0)
            .lengthCm(request.lengthCm())
            .widthCm(request.widthCm())
            .heightCm(request.heightCm())
            .build();
    }

    public PackingItemResult toPackingItem(AdminUpdateProductSkuRequest request) {
        return PackingItemResult.builder()
            .weightKg(request.weightGrams() / 1000.0)
            .lengthCm(request.lengthCm())
            .widthCm(request.widthCm())
            .heightCm(request.heightCm())
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
