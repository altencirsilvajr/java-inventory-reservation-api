package dev.altencir.inventory.infrastructure;

import dev.altencir.inventory.domain.Reservation;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
    Optional<Reservation> findByIdempotencyKey(String key);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select reservation from Reservation reservation where reservation.id = :id")
    Optional<Reservation> findByIdForUpdate(@Param("id") UUID id);

    @Query("select reservation.id from Reservation reservation where reservation.status = dev.altencir.inventory.domain.ReservationStatus.PENDING and reservation.expiresAt <= :now order by reservation.expiresAt")
    List<UUID> findExpiredIds(@Param("now") Instant now);

    @Query(value = "select pg_advisory_xact_lock(hashtextextended(:key, 0))", nativeQuery = true)
    Object acquireIdempotencyLock(@Param("key") String key);
}
