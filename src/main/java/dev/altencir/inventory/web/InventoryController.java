package dev.altencir.inventory.web;

import dev.altencir.inventory.application.AvailabilityView;
import dev.altencir.inventory.application.InventoryReservationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory/items")
public class InventoryController {
    private final InventoryReservationService service;

    public InventoryController(InventoryReservationService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<AvailabilityView> create(@Valid @RequestBody CreateItemRequest request) {
        var item = service.createItem(request.sku(), request.initialQuantity());
        return ResponseEntity.created(URI.create("/api/inventory/items/" + item.itemId())).body(item);
    }

    @GetMapping("/{itemId}/availability")
    public AvailabilityView availability(@PathVariable UUID itemId) { return service.availability(itemId); }

    public record CreateItemRequest(@NotBlank @Size(max = 80) String sku, @Min(0) int initialQuantity) {}
}
