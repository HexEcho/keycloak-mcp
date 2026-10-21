package io.github.sakc.keycloak.mcp.domain.error;

public class UserNotFoundException extends KeycloakAdminException {

    public UserNotFoundException(String message, String requestId, String correlationId) {
        super("user_not_found", message, 404, null, requestId, correlationId);
    }
}
