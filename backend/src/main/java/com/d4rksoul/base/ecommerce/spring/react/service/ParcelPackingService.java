package com.d4rksoul.base.ecommerce.spring.react.service;

import com.d4rksoul.base.ecommerce.spring.react.config.ShippingPackingProperties;
import com.d4rksoul.base.ecommerce.spring.react.exception.UnpackableCartException;
import com.d4rksoul.base.ecommerce.spring.react.dto.result.PackedParcelResult;
import com.d4rksoul.base.ecommerce.spring.react.dto.result.PackingItemResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ParcelPackingService {

    private final ShippingPackingProperties packingProperties;

    public List<PackedParcelResult> pack(List<PackingItemResult> items) {
        List<ShippingPackingProperties.Box> catalog = packingProperties.getBoxes().stream()
                .sorted(Comparator.comparingDouble(ShippingPackingProperties.Box::volumeCm3))
                .toList();

        List<PackingItemResult> sortedItems = items.stream()
                .sorted(Comparator.comparingDouble(PackingItemResult::volumeCm3).reversed())
                .toList();

        List<OpenBox> openBoxes = new ArrayList<>();

        for (PackingItemResult item : sortedItems) {
            OpenBox target = openBoxes.stream()
                    .filter(box -> box.canFit(item))
                    .findFirst()
                    .orElse(null);

            if (target == null) {
                ShippingPackingProperties.Box boxDefinition = catalog.stream()
                        .filter(box -> item.weightKg() <= box.maxWeightKg() && item.volumeCm3() <= box.volumeCm3())
                        .findFirst()
                        .orElseThrow(() -> new UnpackableCartException(
                                "No configured box is large enough for an item weighing " + item.weightKg()
                                        + "kg with dimensions " + item.lengthCm() + "x" + item.widthCm()
                                        + "x" + item.heightCm() + "cm."
                        ));

                target = new OpenBox(boxDefinition);
                openBoxes.add(target);
            }

            target.add(item);
        }

        return openBoxes.stream()
                .map(OpenBox::toPackedParcel)
                .toList();
    }

    private static class OpenBox {

        private final ShippingPackingProperties.Box definition;
        private double remainingWeightKg;
        private double remainingVolumeCm3;
        private double packedWeightKg = 0;

        private OpenBox(ShippingPackingProperties.Box definition) {
            this.definition = definition;
            this.remainingWeightKg = definition.maxWeightKg();
            this.remainingVolumeCm3 = definition.volumeCm3();
        }

        boolean canFit(PackingItemResult item) {
            return item.weightKg() <= remainingWeightKg && item.volumeCm3() <= remainingVolumeCm3;
        }

        void add(PackingItemResult item) {
            remainingWeightKg -= item.weightKg();
            remainingVolumeCm3 -= item.volumeCm3();
            packedWeightKg += item.weightKg();
        }

        PackedParcelResult toPackedParcel() {
            return new PackedParcelResult(
                    definition.name(),
                    packedWeightKg,
                    definition.lengthCm(),
                    definition.widthCm(),
                    definition.heightCm()
            );
        }
    }
}
