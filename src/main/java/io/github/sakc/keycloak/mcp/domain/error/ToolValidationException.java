package io.github.sakc.keycloak.mcp.domain.error;

public class ToolValidationException extends KeycloakAdminException {

    public ToolValidationException(String message, String correlationId) {
        super("validation_error", message, 400, null, null, correlationId);
    }
}
