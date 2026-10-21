package io.github.sakc.keycloak.mcp.config;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mcp.security")
public record ToolSecurityProperties(
    boolean enforceHttpJwt,
    boolean allowAnonymousStdio,
    Set<String> dangerousTools,
    Set<String> allowedDangerousTools,
    Map<String, List<String>> toolRoles,
    RateLimit rateLimit
) {

    public ToolSecurityProperties {
        dangerousTools = dangerousTools == null ? Set.of() : Set.copyOf(dangerousTools);
        allowedDangerousTools = allowedDangerousTools == null ? Set.of() : Set.copyOf(allowedDangerousTools);
        toolRoles = toolRoles == null ? Map.of() : Map.copyOf(toolRoles);
        rateLimit = rateLimit == null ? new RateLimit(true, 120, Duration.ofMinutes(1)) : rateLimit;
    }

    public record RateLimit(boolean enabled, int requests, Duration window) {
    }
}
