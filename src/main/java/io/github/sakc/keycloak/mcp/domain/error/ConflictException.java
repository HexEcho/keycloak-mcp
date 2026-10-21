package io.github.sakc.keycloak.mcp.domain.error;

public class ConflictException extends KeycloakAdminException {

    public ConflictException(String message, int httpStatus, String keycloakError, String requestId, String correlationId) {
        super("conflict", message, httpStatus, keycloakError, requestId, correlationId);
    }
}
