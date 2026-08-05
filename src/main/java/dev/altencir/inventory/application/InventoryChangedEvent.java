package dev.altencir.inventory.application;

import java.util.UUID;

public record InventoryChangedEvent(UUID itemId) {}
