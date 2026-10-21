package io.github.sakc.keycloak.mcp.domain.security;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.github.sakc.keycloak.mcp.config.ToolSecurityProperties;
import io.github.sakc.keycloak.mcp.domain.error.RateLimitException;

public class SlidingWindowRateLimiter {

    private final ToolSecurityProperties properties;
    private final Clock clock;
    private final Map<String, ArrayDeque<Instant>> windows = new ConcurrentHashMap<>();

    public SlidingWindowRateLimiter(ToolSecurityProperties properties) {
        this(properties, Clock.systemUTC());
    }

    SlidingWindowRateLimiter(ToolSecurityProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
    }

    public void acquire(String principal, String toolName, String correlationId) {
        if (!properties.rateLimit().enabled()) {
            return;
        }
        String key = principal + ':' + toolName;
        ArrayDeque<Instant> deque = windows.computeIfAbsent(key, ignored -> new ArrayDeque<>());
        synchronized (deque) {
            Instant threshold = Instant.now(clock).minus(properties.rateLimit().window());
            while (!deque.isEmpty() && deque.peekFirst().isBefore(threshold)) {
                deque.removeFirst();
            }
            if (deque.size() >= properties.rateLimit().requests()) {
                throw new RateLimitException("Rate limit exceeded for tool " + toolName, correlationId);
            }
            deque.addLast(Instant.now(clock));
        }
    }
}
