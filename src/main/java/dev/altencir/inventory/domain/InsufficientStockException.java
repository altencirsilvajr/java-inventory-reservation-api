package dev.altencir.inventory.domain;

import java.util.UUID;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(UUID itemId, int requested, int available) {
        super("Item %s has %d units available; %d requested".formatted(itemId, available, requested));
    }
}
