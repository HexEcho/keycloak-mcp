package io.github.sakc.keycloak.mcp.tool.provider;

import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.sakc.keycloak.mcp.config.ToolSecurityProperties;
import io.github.sakc.keycloak.mcp.domain.dto.user.UserRepresentation;
import io.github.sakc.keycloak.mcp.domain.observability.AuditService;
import io.github.sakc.keycloak.mcp.domain.security.SlidingWindowRateLimiter;
import io.github.sakc.keycloak.mcp.domain.security.ToolAuthorizationService;
import io.github.sakc.keycloak.mcp.domain.service.KeycloakPathBuilder;
import io.github.sakc.keycloak.mcp.domain.service.ToolExecutionTemplate;
import io.github.sakc.keycloak.mcp.domain.service.UserAdminService;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.tracing.Tracer;
import jakarta.validation.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class UserToolProviderIntegrationTest {

    private UserToolProvider provider;

    @BeforeEach
    void setUp() {
        ToolSecurityProperties securityProperties = new ToolSecurityProperties(
            false,
            true,
            Set.of(),
            Set.of(),
            Map.of(),
            new ToolSecurityProperties.RateLimit(false, 1000, java.time.Duration.ofMinutes(1))
        );
        ToolAuthorizationService authorizationService = new ToolAuthorizationService(securityProperties);
        SlidingWindowRateLimiter rateLimiter = new SlidingWindowRateLimiter(securityProperties);
        AuditService auditService = new AuditService();

        ToolExecutionTemplate template = new ToolExecutionTemplate(
            Validation.buildDefaultValidatorFactory().getValidator(),
            authorizationService,
            rateLimiter,
            auditService,
            new SimpleMeterRegistry(),
            Tracer.NOOP
        );

        UserAdminService userAdminService = new UserAdminService(null, new KeycloakPathBuilder()) {
            @Override
            public Map<String, Object> createUser(String realm, UserRepresentation payload) {
                return Map.of("userId", "abc-123", "status", 201, "realm", realm);
            }
        };

        provider = new UserToolProvider(userAdminService, template);
    }

    @Test
    void shouldInvokeCreateUserThroughToolCallback() {
        var handler = provider.handlers().stream()
            .filter(tool -> tool.name().equals("create_user"))
            .findFirst()
            .orElseThrow();

        var request = new UserToolProvider.CreateUserRequest(
            "demo",
            new UserToolProvider.CreateUserPayload("jane", null, null, "jane@example.com", true, null, null, null, null, null)
        );
        @SuppressWarnings("unchecked")
        io.github.sakc.keycloak.mcp.tool.KeycloakToolHandler<UserToolProvider.CreateUserRequest> typedHandler =
            (io.github.sakc.keycloak.mcp.tool.KeycloakToolHandler<UserToolProvider.CreateUserRequest>) handler;
        Object result = typedHandler.handle(request, null);

        assertTrue(result instanceof io.github.sakc.keycloak.mcp.api.ToolResponse);
        io.github.sakc.keycloak.mcp.api.ToolResponse response = (io.github.sakc.keycloak.mcp.api.ToolResponse) result;
        assertTrue(response.success());
        assertTrue(response.data().toString().contains("abc-123"));
    }
}
