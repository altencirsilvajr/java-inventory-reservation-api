package dev.altencir.inventory.application;

import dev.altencir.inventory.domain.ReservationStatus;
import dev.altencir.inventory.infrastructure.InventoryItemRepository;
import dev.altencir.inventory.infrastructure.ReservationRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservationExpirationProcessor {
    private final ReservationRepository reservations;
    private final InventoryItemRepository items;
    private final ApplicationEventPublisher events;
    private final Clock clock;

    public ReservationExpirationProcessor(ReservationRepository reservations, InventoryItemRepository items,
            ApplicationEventPublisher events, Clock clock) {
        this.reservations = reservations;
        this.items = items;
        this.events = events;
        this.clock = clock;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean expire(UUID id) {
        var reservation = reservations.findByIdForUpdate(id).orElse(null);
        var now = Instant.now(clock);
        if (reservation == null || reservation.status() != ReservationStatus.PENDING || now.isBefore(reservation.expiresAt())) return false;
        var item = items.findByIdForUpdate(reservation.itemId())
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found"));
        reservation.expire(now);
        item.release(reservation.quantity());
        events.publishEvent(new InventoryChangedEvent(item.id()));
        return true;
    }
}
