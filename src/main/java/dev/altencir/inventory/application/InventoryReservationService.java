package dev.altencir.inventory.application;

import dev.altencir.inventory.domain.InventoryItem;
import dev.altencir.inventory.domain.Reservation;
import dev.altencir.inventory.infrastructure.InventoryItemRepository;
import dev.altencir.inventory.infrastructure.ReservationRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryReservationService {
    private static final Duration RESERVATION_TTL = Duration.ofMinutes(5);

    private final InventoryItemRepository items;
    private final ReservationRepository reservations;
    private final AvailabilityCache cache;
    private final ApplicationEventPublisher events;
    private final Clock clock;

    public InventoryReservationService(InventoryItemRepository items, ReservationRepository reservations,
            AvailabilityCache cache, ApplicationEventPublisher events, Clock clock) {
        this.items = items;
        this.reservations = reservations;
        this.cache = cache;
        this.events = events;
        this.clock = clock;
    }

    @Transactional
    public AvailabilityView createItem(String sku, int initialQuantity) {
        var item = items.save(InventoryItem.create(UUID.randomUUID(), sku, initialQuantity));
        events.publishEvent(new InventoryChangedEvent(item.id()));
        return AvailabilityView.from(item);
    }

    @Transactional(readOnly = true)
    public AvailabilityView availability(UUID itemId) {
        return cache.get(itemId).orElseGet(() -> {
            var view = AvailabilityView.from(items.findById(itemId)
                    .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found")));
            cache.put(view);
            return view;
        });
    }

    @Transactional
    public ReserveResult reserve(UUID itemId, int quantity, String idempotencyKey) {
        reservations.acquireIdempotencyLock(idempotencyKey);
        var existing = reservations.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            var reservation = existing.get();
            if (!reservation.itemId().equals(itemId) || reservation.quantity() != quantity) throw new IdempotencyConflictException();
            return new ReserveResult(ReservationView.from(reservation), true);
        }

        var item = items.findByIdForUpdate(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found"));
        item.reserve(quantity);
        var reservation = Reservation.pending(UUID.randomUUID(), itemId, quantity, idempotencyKey,
                Instant.now(clock).plus(RESERVATION_TTL));
        reservations.save(reservation);
        events.publishEvent(new InventoryChangedEvent(itemId));
        return new ReserveResult(ReservationView.from(reservation), false);
    }

    @Transactional(readOnly = true)
    public ReservationView reservation(UUID id) {
        return ReservationView.from(reservations.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found")));
    }

    @Transactional
    public ReservationView confirm(UUID id) {
        var reservation = lockedReservation(id);
        var item = lockedItem(reservation.itemId());
        reservation.confirm(Instant.now(clock));
        item.confirm(reservation.quantity());
        events.publishEvent(new InventoryChangedEvent(item.id()));
        return ReservationView.from(reservation);
    }

    @Transactional
    public ReservationView cancel(UUID id) {
        var reservation = lockedReservation(id);
        var item = lockedItem(reservation.itemId());
        reservation.cancel(Instant.now(clock));
        item.release(reservation.quantity());
        events.publishEvent(new InventoryChangedEvent(item.id()));
        return ReservationView.from(reservation);
    }

    private Reservation lockedReservation(UUID id) {
        return reservations.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));
    }

    private InventoryItem lockedItem(UUID id) {
        return items.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found"));
    }
}
