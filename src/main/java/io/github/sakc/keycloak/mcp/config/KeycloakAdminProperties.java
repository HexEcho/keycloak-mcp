package io.github.sakc.keycloak.mcp.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "keycloak.admin")
public record KeycloakAdminProperties(
    String baseUrl,
    String adminRealm,
    String tokenPath,
    Auth auth,
    Http http,
    Resilience resilience
) {

    public KeycloakAdminProperties {
        adminRealm = adminRealm == null || adminRealm.isBlank() ? "master" : adminRealm;
        tokenPath = tokenPath == null || tokenPath.isBlank() ? "/realms/%s/protocol/openid-connect/token" : tokenPath;
        auth = auth == null ? new Auth(AuthMode.CLIENT_CREDENTIALS, null, null, null, null, null) : auth;
        http = http == null ? new Http(Duration.ofSeconds(5), Duration.ofSeconds(30), 200, 100, true) : http;
        resilience = resilience == null ? new Resilience(3, Duration.ofMillis(300), Duration.ofSeconds(2), 25, 50, 50) : resilience;
    }

    public enum AuthMode {
        CLIENT_CREDENTIALS,
        PASSWORD,
        TOKEN_EXCHANGE,
        MTLS,
        SERVICE_ACCOUNT
    }

    public record Auth(
        AuthMode mode,
        String clientId,
        String clientSecret,
        String username,
        String password,
        String scope
    ) {
    }

    public record Http(
        Duration connectTimeout,
        Duration responseTimeout,
        int maxConnections,
        int pendingAcquireMaxCount,
        boolean wiretap
    ) {
    }

    public record Resilience(
        int maxAttempts,
        Duration retryBackoff,
        Duration timeout,
        int failureRateThreshold,
        int slowCallRateThreshold,
        int maxConcurrentCalls
    ) {
    }
}
