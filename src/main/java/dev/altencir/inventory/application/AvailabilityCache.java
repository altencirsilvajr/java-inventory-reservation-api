package dev.altencir.inventory.application;

import java.util.Optional;
import java.util.UUID;

public interface AvailabilityCache {
    Optional<AvailabilityView> get(UUID itemId);
    void put(AvailabilityView availability);
    void evict(UUID itemId);
}
