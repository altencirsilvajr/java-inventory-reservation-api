package dev.altencir.inventory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.altencir.inventory.application.AvailabilityCache;
import dev.altencir.inventory.application.InventoryReservationService;
import dev.altencir.inventory.domain.InsufficientStockException;
import dev.altencir.inventory.infrastructure.InventoryItemRepository;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(properties = "reservation.expiration-interval=3600000")
@Testcontainers(disabledWithoutDocker = true)
class InventoryReservationIntegrationTest {
    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("inventory").withUsername("inventory").withPassword("inventory");

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry properties) {
        properties.add("spring.datasource.url", postgres::getJdbcUrl);
        properties.add("spring.datasource.username", postgres::getUsername);
        properties.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired InventoryReservationService service;
    @Autowired InventoryItemRepository items;
    @MockitoBean AvailabilityCache cache;

    @Test
    void repeatedIdempotencyKeyReturnsOriginalReservationWithoutConsumingTwice() {
        var item = service.createItem("IDEMPOTENT-" + UUID.randomUUID(), 5);
        var key = "replay-" + UUID.randomUUID();
        var original = service.reserve(item.itemId(), 2, key);
        var repeated = service.reserve(item.itemId(), 2, key);

        assertThat(original.replayed()).isFalse();
        assertThat(repeated.replayed()).isTrue();
        assertThat(repeated.reservation().id()).isEqualTo(original.reservation().id());
        assertThat(items.findById(item.itemId()).orElseThrow().availableQuantity()).isEqualTo(3);
    }

    @Test
    void concurrentClaimsForLastUnitsAcceptExactlyOne() throws Exception {
        var item = service.createItem("RACE-" + UUID.randomUUID(), 2);
        var start = new CountDownLatch(1);
        var executor = Executors.newFixedThreadPool(2);
        try {
            var first = executor.submit(() -> reserveAfter(start, item.itemId(), "race-a-" + UUID.randomUUID()));
            var second = executor.submit(() -> reserveAfter(start, item.itemId(), "race-b-" + UUID.randomUUID()));
            start.countDown();
            var outcomes = java.util.List.of(first.get(), second.get());

            assertThat(outcomes).containsExactlyInAnyOrder("accepted", "insufficient");
            var stored = items.findById(item.itemId()).orElseThrow();
            assertThat(stored.availableQuantity()).isZero();
            assertThat(stored.reservedQuantity()).isEqualTo(2);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void confirmConsumesAndCancelReleasesReservedStock() {
        var item = service.createItem("FLOW-" + UUID.randomUUID(), 5);
        var confirmed = service.reserve(item.itemId(), 2, "confirm-" + UUID.randomUUID());
        var cancelled = service.reserve(item.itemId(), 2, "cancel-" + UUID.randomUUID());

        service.confirm(confirmed.reservation().id());
        service.cancel(cancelled.reservation().id());

        var availability = items.findById(item.itemId()).orElseThrow();
        assertThat(availability.onHandQuantity()).isEqualTo(3);
        assertThat(availability.reservedQuantity()).isZero();
        assertThatThrownBy(() -> service.cancel(confirmed.reservation().id())).isInstanceOf(RuntimeException.class);
    }

    private String reserveAfter(CountDownLatch start, UUID itemId, String key) throws InterruptedException {
        start.await();
        try {
            service.reserve(itemId, 2, key);
            return "accepted";
        } catch (InsufficientStockException failure) {
            return "insufficient";
        }
    }
}
