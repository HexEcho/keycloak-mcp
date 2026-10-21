package io.github.sakc.keycloak.mcp.domain.error;

public class AuthenticationException extends KeycloakAdminException {

    public AuthenticationException(String message, int httpStatus, String keycloakError, String requestId, String correlationId) {
        super("authentication_error", message, httpStatus, keycloakError, requestId, correlationId);
    }
}
