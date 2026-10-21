package io.github.sakc.keycloak.mcp.domain.error;

public class KeycloakAdminException extends RuntimeException {

    private final String code;
    private final int httpStatus;
    private final String keycloakError;
    private final String requestId;
    private final String correlationId;

    public KeycloakAdminException(String code, String message, int httpStatus, String keycloakError, String requestId, String correlationId) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
        this.keycloakError = keycloakError;
        this.requestId = requestId;
        this.correlationId = correlationId;
    }

    public String code() {
        return code;
    }

    public int httpStatus() {
        return httpStatus;
    }

    public String keycloakError() {
        return keycloakError;
    }

    public String requestId() {
        return requestId;
    }

    public String correlationId() {
        return correlationId;
    }
}
