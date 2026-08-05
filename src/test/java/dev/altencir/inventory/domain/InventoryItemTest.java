package dev.altencir.inventory.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class InventoryItemTest {
    @Test
    void reservationNeverMakesAvailabilityNegative() {
        var item = InventoryItem.create(UUID.randomUUID(), "SKU-1", 5);

        item.reserve(3);

        assertThat(item.availableQuantity()).isEqualTo(2);
        assertThatThrownBy(() -> item.reserve(3)).isInstanceOf(InsufficientStockException.class);
        assertThat(item.availableQuantity()).isEqualTo(2);
    }

    @Test
    void cancellingReleasesReservedUnitsAndConfirmingConsumesThem() {
        var cancelled = InventoryItem.create(UUID.randomUUID(), "CANCEL", 5);
        cancelled.reserve(2);
        cancelled.release(2);
        assertThat(cancelled.availableQuantity()).isEqualTo(5);

        var confirmed = InventoryItem.create(UUID.randomUUID(), "CONFIRM", 5);
        confirmed.reserve(2);
        confirmed.confirm(2);
        assertThat(confirmed.onHandQuantity()).isEqualTo(3);
        assertThat(confirmed.availableQuantity()).isEqualTo(3);
    }
}
