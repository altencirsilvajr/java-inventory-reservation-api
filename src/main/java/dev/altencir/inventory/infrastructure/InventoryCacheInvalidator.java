package dev.altencir.inventory.infrastructure;

import dev.altencir.inventory.application.AvailabilityCache;
import dev.altencir.inventory.application.InventoryChangedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class InventoryCacheInvalidator {
    private final AvailabilityCache cache;

    public InventoryCacheInvalidator(AvailabilityCache cache) { this.cache = cache; }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void inventoryChanged(InventoryChangedEvent event) { cache.evict(event.itemId()); }
}
