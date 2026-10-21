package io.github.sakc.keycloak.mcp.domain.error;

public class KeycloakServerException extends KeycloakAdminException {

    public KeycloakServerException(String message, int httpStatus, String keycloakError, String requestId, String correlationId) {
        super("keycloak_server_error", message, httpStatus, keycloakError, requestId, correlationId);
    }
}
