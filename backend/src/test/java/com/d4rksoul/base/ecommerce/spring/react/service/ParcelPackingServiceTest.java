package com.d4rksoul.base.ecommerce.spring.react.service;

import com.d4rksoul.base.ecommerce.spring.react.config.ShippingPackingProperties;
import com.d4rksoul.base.ecommerce.spring.react.dto.result.PackedParcelItemsResult;
import com.d4rksoul.base.ecommerce.spring.react.dto.result.PackingItemResult;
import com.d4rksoul.base.ecommerce.spring.react.exception.UnpackableCartException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.within;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class ParcelPackingServiceTest {

    private final ParcelPackingService service = new ParcelPackingService(
        new ShippingPackingProperties(List.of(
            new ShippingPackingProperties.Box(
                "Small", 2, 20, 15, 10
            ),
            new ShippingPackingProperties.Box(
                "Medium", 5, 35, 25, 15
            ),
            new ShippingPackingProperties.Box(
                "Big", 10, 45, 35, 25
            )
        ))
    );

    @Test
    @DisplayName("returns no parcels for an empty cart")
    void returnsNoParcelsForEmptyCart() {
        assertThat(service.pack(List.of())).isEmpty();
    }

    @Test
    @DisplayName("shrinks a single light item to the smallest box")
    void shrinksSingleItemToSmallestBox() {
        List<PackedParcelItemsResult> result = service.pack(List.of(
            item(0.5, 10, 10, 10)
        ));

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().parcel().boxName()).isEqualTo("Small");
        assertThat(result.getFirst().parcel().weightKg()).isEqualTo(0.5);
        assertThat(result.getFirst().parcel().lengthCm()).isEqualTo(20);
        assertThat(result.getFirst().parcel().widthCm()).isEqualTo(15);
        assertThat(result.getFirst().parcel().heightCm()).isEqualTo(10);
    }

    @Test
    @DisplayName("keeps the smallest box when the weight is exactly at its limit")
    void keepsSmallestBoxAtWeightLimit() {
        List<PackedParcelItemsResult> result = service.pack(List.of(
            item(1, 10, 10, 10),
            item(1, 10, 10, 10)
        ));

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().items()).hasSize(2);
        assertThat(result.getFirst().parcel().boxName()).isEqualTo("Small");
    }

    @Test
    @DisplayName("moves to the next box size when the weight exceeds the smaller one")
    void movesToNextBoxWhenWeightExceedsLimit() {
        List<PackedParcelItemsResult> result = service.pack(List.of(
            item(1.1, 10, 10, 10),
            item(1, 10, 10, 10)
        ));

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().parcel().boxName()).isEqualTo("Medium");
    }

    @Test
    @DisplayName("puts a large item and small items in the same box when they fit together")
    void packsLargeAndSmallItemsTogether() {
        List<PackedParcelItemsResult> result = service.pack(List.of(
            item(0.5, 10, 10, 10),
            item(3, 30, 30, 20))
        );

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().items()).hasSize(2);
        assertThat(result.getFirst().parcel().boxName()).isEqualTo("Big");
        assertThat(result.getFirst().parcel().weightKg()).isEqualTo(3.5);
    }

    @Test
    @DisplayName("keeps every item in the result when everything fits in one parcel")
    void keepsEveryItem() {
        List<PackedParcelItemsResult> result = service.pack(List.of(
            item(0.4, 10, 10, 10),
            item(1.5, 20, 20, 10),
            item(3, 30, 20, 20),
            item(2, 25, 25, 20),
            item(0.2, 5, 5, 5)
        ));

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().items()).hasSize(5);
        assertThat(result.getFirst().parcel().weightKg()).isCloseTo(7.1, within(1e-9));
    }

    @Test
    @DisplayName("splits items into several parcels when one box cannot hold them")
    void splitsIntoSeveralParcels() {
        List<PackedParcelItemsResult> result = service.pack(List.of(
            item(6, 10, 10, 10),
            item(6, 10, 10, 10),
            item(6, 10, 10, 10)
        ));

        assertThat(result).hasSize(3);
        assertThat(result).allSatisfy(parcel -> {
            assertThat(parcel.parcel().boxName()).isEqualTo("Big");
            assertThat(parcel.items()).hasSize(1);
        });
    }

    @Test
    @DisplayName("rejects an item heavier than the largest box")
    void rejectsItemTooHeavy() {
        assertThatThrownBy(() -> service.pack(List.of(
            item(11, 10, 10, 10))
        ))
        .isInstanceOf(UnpackableCartException.class)
        .hasMessageContaining("No configured box is large enough");
    }

    @Test
    @DisplayName("rejects an item larger than the largest box")
    void rejectsItemTooLarge() {
        assertThatThrownBy(() -> service.pack(List.of(
            item(1, 50, 40, 30))
        ))
        .isInstanceOf(UnpackableCartException.class)
        .hasMessageContaining("No configured box is large enough");
    }

    private static PackingItemResult item(
        double weightKg, double lengthCm, double widthCm, double heightCm
    ) {
        return PackingItemResult.builder()
            .productSkuId(1L)
            .weightKg(weightKg)
            .lengthCm(lengthCm)
            .widthCm(widthCm)
            .heightCm(heightCm)
            .build();
    }
}
