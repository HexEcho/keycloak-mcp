package io.github.sakc.keycloak.mcp.domain.error;

public class RealmNotFoundException extends KeycloakAdminException {

    public RealmNotFoundException(String message, String requestId, String correlationId) {
        super("realm_not_found", message, 404, null, requestId, correlationId);
    }
}
