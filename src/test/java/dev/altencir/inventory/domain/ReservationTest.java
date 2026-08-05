package dev.altencir.inventory.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ReservationTest {
    @Test
    void pendingReservationCanTransitionOnlyOnce() {
        var reservation = Reservation.pending(UUID.randomUUID(), UUID.randomUUID(), 2, "key", Instant.parse("2026-08-05T10:00:00Z"));

        reservation.confirm(Instant.parse("2026-08-05T09:55:00Z"));

        assertThat(reservation.status()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThatThrownBy(() -> reservation.cancel(Instant.parse("2026-08-05T09:56:00Z")))
                .isInstanceOf(InvalidReservationTransitionException.class);
    }

    @Test
    void expiredReservationCannotBeConfirmed() {
        var reservation = Reservation.pending(UUID.randomUUID(), UUID.randomUUID(), 1, "key", Instant.parse("2026-08-05T10:00:00Z"));
        assertThatThrownBy(() -> reservation.confirm(Instant.parse("2026-08-05T10:00:01Z")))
                .isInstanceOf(InvalidReservationTransitionException.class);
    }
}
