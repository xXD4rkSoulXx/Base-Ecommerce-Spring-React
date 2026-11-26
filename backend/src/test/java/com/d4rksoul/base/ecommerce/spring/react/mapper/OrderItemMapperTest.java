package com.d4rksoul.base.ecommerce.spring.react.mapper;

import com.d4rksoul.base.ecommerce.spring.react.dto.response.OrderItemResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.CartItem;
import com.d4rksoul.base.ecommerce.spring.react.entity.Order;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderItem;
import com.d4rksoul.base.ecommerce.spring.react.entity.Product;
import com.d4rksoul.base.ecommerce.spring.react.entity.ProductSku;
import com.d4rksoul.base.ecommerce.spring.react.enums.ProductColor;
import com.d4rksoul.base.ecommerce.spring.react.enums.ProductSize;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class OrderItemMapperTest {

    private final OrderItemMapper mapper = new OrderItemMapper();

    @Test
    @DisplayName("snapshots the product discount when the SKU has none")
    void usesProductDiscountWhenSkuHasNone() {
        OrderItem item = mapper.toEntity(
            new Order(),
            cartItem("100.00", 20, 0, 1),
            null
        );

        assertThat(item.getDiscountPercentage()).isEqualTo(20);
    }

    @Test
    @DisplayName("snapshots the SKU discount over the product discount when the SKU has one")
    void skuDiscountOverridesProductDiscount() {
        OrderItem item = mapper.toEntity(
            new Order(),
            cartItem("100.00", 20, 35, 1),
            null
        );

        assertThat(item.getDiscountPercentage()).isEqualTo(35);
    }

    @Test
    @DisplayName("snapshots the price and quantity at order time")
    void snapshotsPriceAndQuantity() {
        OrderItem item = mapper.toEntity(
            new Order(),
            cartItem("49.90", 0, 0, 3),
            "cover.jpg"
        );

        assertThat(item.getPrice()).isEqualByComparingTo("49.90");
        assertThat(item.getQuantity()).isEqualTo(3);
        assertThat(item.getImagePath()).isEqualTo("cover.jpg");
    }

    @Test
    @DisplayName("keeps the full price when there is no discount")
    void fullPriceWithoutDiscount() {
        OrderItemResponse response = mapper.toDto(
            orderItem("59.90", 0, 2), 0
        );

        assertThat(response.unitPrice()).isEqualByComparingTo("59.90");
        assertThat(response.lineTotal()).isEqualByComparingTo("119.80");
    }

    @Test
    @DisplayName("applies the percentage discount to the unit price")
    void appliesDiscountToUnitPrice() {
        OrderItemResponse response = mapper.toDto(
            orderItem("80.00", 25, 3), 0
        );

        assertThat(response.unitPrice()).isEqualByComparingTo("60.00");
        assertThat(response.lineTotal()).isEqualByComparingTo("180.00");
    }

    @Test
    @DisplayName("rounds the unit price half up to 2 decimals")
    void roundsUnitPriceHalfUp() {
        assertThat(mapper.toDto(
            orderItem("19.99", 15, 1), 0)
        .unitPrice()).isEqualByComparingTo("16.99");
        assertThat(mapper.toDto(
            orderItem("10.05", 50, 1), 0)
        .unitPrice()).isEqualByComparingTo("5.03");
    }

    @Test
    @DisplayName("multiplies the already rounded unit price by the quantity")
    void lineTotalUsesRoundedUnitPrice() {
        OrderItemResponse response = mapper.toDto(
            orderItem("19.99", 15, 3), 0
        );

        assertThat(response.lineTotal()).isEqualByComparingTo("50.97");
    }

    @Test
    @DisplayName("gives a free item with a 100% discount")
    void hundredPercentDiscountIsFree() {
        OrderItemResponse response = mapper.toDto(
            orderItem("30.00", 100, 4), 0
        );

        assertThat(response.unitPrice()).isEqualByComparingTo("0.00");
        assertThat(response.lineTotal()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("passes the returnable quantity through")
    void passesReturnableQuantity() {
        OrderItemResponse response = mapper.toDto(
            orderItem("30.00", 0, 4), 2
        );

        assertThat(response.returnableQuantity()).isEqualTo(2);
    }

    private static CartItem cartItem(
        String price, int productDiscount, int skuDiscount, int quantity
    ) {
        Product product = Product.builder()
            .ref("REF-1")
            .name("Jacket")
            .price(new BigDecimal(price))
            .discountPercentage(productDiscount)
            .build();

        ProductSku sku = ProductSku.builder()
            .product(product)
            .size(ProductSize.M)
            .color(ProductColor.BLACK)
            .discountPercentage(skuDiscount)
            .build();

        return CartItem.builder()
            .productSku(sku)
            .quantity(quantity)
            .build();
    }

    private static OrderItem orderItem(String price, int discountPercentage, int quantity) {
        Product product = Product.builder()
            .ref("REF-1")
            .build();

        ProductSku sku = ProductSku.builder()
            .product(product)
            .build();

        return OrderItem.builder()
            .id(1L)
            .productSku(sku)
            .productName("Jacket")
            .size(ProductSize.M)
            .color(ProductColor.BLACK)
            .price(new BigDecimal(price))
            .discountPercentage(discountPercentage)
            .quantity(quantity)
            .build();
    }
}
