package dev.altencir.inventory.application;

import dev.altencir.inventory.domain.InventoryItem;
import java.util.UUID;

public record AvailabilityView(UUID itemId, String sku, int onHandQuantity, int reservedQuantity, int availableQuantity) {
    public static AvailabilityView from(InventoryItem item) {
        return new AvailabilityView(item.id(), item.sku(), item.onHandQuantity(), item.reservedQuantity(), item.availableQuantity());
    }
}
