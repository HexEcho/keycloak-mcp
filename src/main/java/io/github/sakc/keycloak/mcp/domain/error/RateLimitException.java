package io.github.sakc.keycloak.mcp.domain.error;

public class RateLimitException extends KeycloakAdminException {

    public RateLimitException(String message, String correlationId) {
        super("rate_limited", message, 429, null, null, correlationId);
    }
}
