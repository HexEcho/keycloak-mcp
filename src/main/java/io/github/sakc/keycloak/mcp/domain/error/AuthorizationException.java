package io.github.sakc.keycloak.mcp.domain.error;

public class AuthorizationException extends KeycloakAdminException {

    public AuthorizationException(String message, int httpStatus, String keycloakError, String requestId, String correlationId) {
        super("authorization_error", message, httpStatus, keycloakError, requestId, correlationId);
    }
}
