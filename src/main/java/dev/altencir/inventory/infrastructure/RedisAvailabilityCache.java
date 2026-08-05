package dev.altencir.inventory.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.altencir.inventory.application.AvailabilityCache;
import dev.altencir.inventory.application.AvailabilityView;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisAvailabilityCache implements AvailabilityCache {
    private static final Logger log = LoggerFactory.getLogger(RedisAvailabilityCache.class);
    private static final Duration TTL = Duration.ofSeconds(30);
    private final StringRedisTemplate redis;
    private final ObjectMapper mapper;

    public RedisAvailabilityCache(StringRedisTemplate redis, ObjectMapper mapper) {
        this.redis = redis;
        this.mapper = mapper;
    }

    @Override
    public Optional<AvailabilityView> get(UUID itemId) {
        try {
            var value = redis.opsForValue().get(key(itemId));
            return value == null ? Optional.empty() : Optional.of(mapper.readValue(value, AvailabilityView.class));
        } catch (Exception failure) {
            log.warn("availability_cache_read_failed itemId={}", itemId, failure);
            return Optional.empty();
        }
    }

    @Override
    public void put(AvailabilityView availability) {
        try {
            redis.opsForValue().set(key(availability.itemId()), mapper.writeValueAsString(availability), TTL);
        } catch (JsonProcessingException | RuntimeException failure) {
            log.warn("availability_cache_write_failed itemId={}", availability.itemId(), failure);
        }
    }

    @Override
    public void evict(UUID itemId) {
        try { redis.delete(key(itemId)); }
        catch (RuntimeException failure) { log.warn("availability_cache_evict_failed itemId={}", itemId, failure); }
    }

    private static String key(UUID itemId) { return "inventory:availability:" + itemId; }
}
