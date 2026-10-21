package io.github.sakc.keycloak.mcp.api;

import java.time.Instant;

public record ToolResponse(
    boolean success,
    String operation,
    String realm,
    String correlationId,
    String traceId,
    long durationMs,
    Instant timestamp,
    Object data,
    ToolError error
) {

    public static ToolResponse success(String operation, String realm, String correlationId, String traceId, long durationMs, Object data) {
        return new ToolResponse(true, operation, realm, correlationId, traceId, durationMs, Instant.now(), data, null);
    }

    public static ToolResponse failure(String operation, String realm, String correlationId, String traceId, long durationMs, ToolError error) {
        return new ToolResponse(false, operation, realm, correlationId, traceId, durationMs, Instant.now(), null, error);
    }
}
