package dev.altencir.inventory.web;

import dev.altencir.inventory.application.InventoryReservationService;
import dev.altencir.inventory.application.ReservationExpirationCoordinator;
import dev.altencir.inventory.application.ReservationView;
import dev.altencir.inventory.application.ReserveResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {
    private final InventoryReservationService service;
    private final ReservationExpirationCoordinator expiration;

    public ReservationController(InventoryReservationService service, ReservationExpirationCoordinator expiration) {
        this.service = service;
        this.expiration = expiration;
    }

    @PostMapping
    public ReserveResult reserve(@Valid @RequestBody ReserveRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey) {
        if (idempotencyKey.isBlank() || idempotencyKey.length() > 120) throw new IllegalArgumentException("Invalid Idempotency-Key");
        return service.reserve(request.itemId(), request.quantity(), idempotencyKey);
    }

    @GetMapping("/{reservationId}")
    public ReservationView get(@PathVariable UUID reservationId) { return service.reservation(reservationId); }

    @PostMapping("/{reservationId}/confirm")
    public ReservationView confirm(@PathVariable UUID reservationId) { return service.confirm(reservationId); }

    @PostMapping("/{reservationId}/cancel")
    public ReservationView cancel(@PathVariable UUID reservationId) { return service.cancel(reservationId); }

    @PostMapping("/expire")
    public Map<String, Integer> expire() { return Map.of("expired", expiration.expirePending()); }

    public record ReserveRequest(@NotNull UUID itemId, @Min(1) int quantity) {}
}
