package dev.altencir.inventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.util.UUID;

@Entity
@Table(name = "inventory_items")
public class InventoryItem {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 80)
    private String sku;

    @Column(name = "on_hand_quantity", nullable = false)
    private int onHandQuantity;

    @Column(name = "reserved_quantity", nullable = false)
    private int reservedQuantity;

    @Version
    private long version;

    protected InventoryItem() {}

    private InventoryItem(UUID id, String sku, int onHandQuantity) {
        if (id == null) throw new IllegalArgumentException("id is required");
        if (sku == null || sku.isBlank()) throw new IllegalArgumentException("sku is required");
        if (onHandQuantity < 0) throw new IllegalArgumentException("initial quantity cannot be negative");
        this.id = id;
        this.sku = sku.trim().toUpperCase();
        this.onHandQuantity = onHandQuantity;
    }

    public static InventoryItem create(UUID id, String sku, int quantity) {
        return new InventoryItem(id, sku, quantity);
    }

    public void reserve(int quantity) {
        requirePositive(quantity);
        if (availableQuantity() < quantity) throw new InsufficientStockException(id, quantity, availableQuantity());
        reservedQuantity += quantity;
    }

    public void release(int quantity) {
        requirePositive(quantity);
        if (reservedQuantity < quantity) throw new IllegalStateException("cannot release more than reserved quantity");
        reservedQuantity -= quantity;
    }

    public void confirm(int quantity) {
        requirePositive(quantity);
        if (reservedQuantity < quantity) throw new IllegalStateException("cannot confirm more than reserved quantity");
        reservedQuantity -= quantity;
        onHandQuantity -= quantity;
    }

    private static void requirePositive(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("quantity must be positive");
    }

    public UUID id() { return id; }
    public String sku() { return sku; }
    public int onHandQuantity() { return onHandQuantity; }
    public int reservedQuantity() { return reservedQuantity; }
    public int availableQuantity() { return onHandQuantity - reservedQuantity; }
}
