package dev.altencir.inventory.application;

import dev.altencir.inventory.domain.Reservation;
import dev.altencir.inventory.domain.ReservationStatus;
import java.time.Instant;
import java.util.UUID;

public record ReservationView(UUID id, UUID itemId, int quantity, ReservationStatus status, Instant expiresAt, Instant completedAt) {
    public static ReservationView from(Reservation reservation) {
        return new ReservationView(reservation.id(), reservation.itemId(), reservation.quantity(), reservation.status(), reservation.expiresAt(), reservation.completedAt());
    }
}
