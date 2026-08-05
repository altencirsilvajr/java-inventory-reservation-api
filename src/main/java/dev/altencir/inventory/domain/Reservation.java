package dev.altencir.inventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reservations")
public class Reservation {
    @Id private UUID id;
    @Column(name = "item_id", nullable = false) private UUID itemId;
    @Column(nullable = false) private int quantity;
    @Column(name = "idempotency_key", nullable = false, unique = true, length = 120) private String idempotencyKey;
    @Column(name = "expires_at", nullable = false) private Instant expiresAt;
    @Column(name = "completed_at") private Instant completedAt;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16) private ReservationStatus status;
    @Version private long version;

    protected Reservation() {}

    private Reservation(UUID id, UUID itemId, int quantity, String key, Instant expiresAt) {
        if (id == null || itemId == null || expiresAt == null) throw new IllegalArgumentException("reservation fields are required");
        if (quantity <= 0) throw new IllegalArgumentException("quantity must be positive");
        if (key == null || key.isBlank()) throw new IllegalArgumentException("idempotency key is required");
        this.id = id;
        this.itemId = itemId;
        this.quantity = quantity;
        this.idempotencyKey = key;
        this.expiresAt = expiresAt;
        this.status = ReservationStatus.PENDING;
    }

    public static Reservation pending(UUID id, UUID itemId, int quantity, String key, Instant expiresAt) {
        return new Reservation(id, itemId, quantity, key, expiresAt);
    }

    public void confirm(Instant now) {
        requirePending();
        if (now.isAfter(expiresAt)) throw new InvalidReservationTransitionException("expired reservation cannot be confirmed");
        status = ReservationStatus.CONFIRMED;
        completedAt = now;
    }

    public void cancel(Instant now) {
        requirePending();
        status = ReservationStatus.CANCELLED;
        completedAt = now;
    }

    public void expire(Instant now) {
        requirePending();
        if (now.isBefore(expiresAt)) throw new InvalidReservationTransitionException("reservation has not expired");
        status = ReservationStatus.EXPIRED;
        completedAt = now;
    }

    private void requirePending() {
        if (status != ReservationStatus.PENDING) throw new InvalidReservationTransitionException("reservation is already " + status);
    }

    public UUID id() { return id; }
    public UUID itemId() { return itemId; }
    public int quantity() { return quantity; }
    public String idempotencyKey() { return idempotencyKey; }
    public Instant expiresAt() { return expiresAt; }
    public Instant completedAt() { return completedAt; }
    public ReservationStatus status() { return status; }
}
