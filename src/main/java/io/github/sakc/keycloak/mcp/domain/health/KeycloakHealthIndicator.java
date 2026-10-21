package io.github.sakc.keycloak.mcp.domain.health;

import io.github.sakc.keycloak.mcp.domain.auth.AdminAccessTokenProvider;
import io.github.sakc.keycloak.mcp.domain.client.KeycloakAdminClient;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class KeycloakHealthIndicator implements HealthIndicator {

    private final AdminAccessTokenProvider tokenProvider;
    private final KeycloakAdminClient keycloakAdminClient;

    public KeycloakHealthIndicator(AdminAccessTokenProvider tokenProvider, KeycloakAdminClient keycloakAdminClient) {
        this.tokenProvider = tokenProvider;
        this.keycloakAdminClient = keycloakAdminClient;
    }

    @Override
    public Health health() {
        try {
            String token = tokenProvider.getAccessToken();
            keycloakAdminClient.get("/admin/realms", null);
            return Health.up().withDetail("tokenRetrieved", token != null && !token.isBlank()).build();
        }
        catch (Exception exception) {
            return Health.down(exception).build();
        }
    }
}
