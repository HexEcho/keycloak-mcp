package io.github.sakc.keycloak.mcp.api;

import java.time.Instant;

public record ToolError(
    String code,
    String message,
    int httpStatus,
    String keycloakError,
    String requestId,
    String correlationId,
    Instant timestamp
) {
}
