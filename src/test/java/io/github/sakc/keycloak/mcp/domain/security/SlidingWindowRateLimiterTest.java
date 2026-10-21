package io.github.sakc.keycloak.mcp.domain.security;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.sakc.keycloak.mcp.config.ToolSecurityProperties;
import io.github.sakc.keycloak.mcp.domain.error.RateLimitException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SlidingWindowRateLimiterTest {

    @Test
    void shouldRejectWhenWindowExceeded() {
        ToolSecurityProperties properties = new ToolSecurityProperties(false, true, Set.of(), Set.of(), Map.of(), new ToolSecurityProperties.RateLimit(true, 2, Duration.ofMinutes(1)));
        Clock clock = Clock.fixed(Instant.parse("2026-07-14T10:15:30Z"), ZoneOffset.UTC);
        SlidingWindowRateLimiter limiter = new SlidingWindowRateLimiter(properties, clock);

        assertDoesNotThrow(() -> limiter.acquire("alice", "create_user", "corr-1"));
        assertDoesNotThrow(() -> limiter.acquire("alice", "create_user", "corr-1"));
        assertThrows(RateLimitException.class, () -> limiter.acquire("alice", "create_user", "corr-1"));
    }
}
