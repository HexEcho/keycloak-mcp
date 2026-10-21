package io.github.sakc.keycloak.mcp.domain.security;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import io.github.sakc.keycloak.mcp.config.ToolSecurityProperties;
import io.github.sakc.keycloak.mcp.domain.error.AuthorizationException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class ToolAuthorizationService {

    private final ToolSecurityProperties properties;

    public ToolAuthorizationService(ToolSecurityProperties properties) {
        this.properties = properties;
    }

    public ToolInvocationContext currentContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            if (!properties.allowAnonymousStdio()) {
                throw new AuthorizationException("Anonymous access is not permitted for MCP tool execution", 403, null, null, null);
            }
            return new ToolInvocationContext("system", List.of("ROLE_SYSTEM"), org.slf4j.MDC.get("correlationId"), currentTraceId());
        }
        List<String> roles = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        return new ToolInvocationContext(authentication.getName(), roles, org.slf4j.MDC.get("correlationId"), currentTraceId());
    }

    public void authorize(String toolName, boolean mutating, ToolInvocationContext context) {
        Map<String, List<String>> toolRoles = properties.toolRoles();
        List<String> requiredRoles = toolRoles.get(toolName);
        if (requiredRoles != null && !requiredRoles.isEmpty()) {
            Set<String> granted = context.roles().stream().collect(Collectors.toSet());
            boolean authorized = requiredRoles.stream().anyMatch(granted::contains);
            if (!authorized) {
                throw new AuthorizationException("Missing required role for tool " + toolName, 403, null, null, context.correlationId());
            }
        }
        if (mutating && properties.dangerousTools().contains(toolName) && !properties.allowedDangerousTools().contains(toolName)) {
            throw new AuthorizationException("Tool " + toolName + " is blocked by dangerous operation policy", 403, null, null, context.correlationId());
        }
    }

    private String currentTraceId() {
        return org.slf4j.MDC.get("traceId");
    }
}
