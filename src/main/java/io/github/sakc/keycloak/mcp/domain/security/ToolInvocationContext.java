package io.github.sakc.keycloak.mcp.domain.security;

import java.util.List;

public record ToolInvocationContext(String principal, List<String> roles, String correlationId, String traceId) {
}
