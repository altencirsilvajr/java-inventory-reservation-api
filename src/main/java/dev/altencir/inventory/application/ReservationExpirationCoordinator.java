package dev.altencir.inventory.application;

import dev.altencir.inventory.infrastructure.ReservationRepository;
import java.time.Clock;
import java.time.Instant;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReservationExpirationCoordinator {
    private final ReservationRepository reservations;
    private final ReservationExpirationProcessor processor;
    private final Clock clock;

    public ReservationExpirationCoordinator(ReservationRepository reservations, ReservationExpirationProcessor processor, Clock clock) {
        this.reservations = reservations;
        this.processor = processor;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${reservation.expiration-interval:30000}")
    public void scheduledExpiration() { expirePending(); }

    public int expirePending() {
        return (int) reservations.findExpiredIds(Instant.now(clock)).stream().filter(processor::expire).count();
    }
}
