package io.github.sakc.keycloak.mcp.domain.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.sakc.keycloak.mcp.config.ToolSecurityProperties;
import io.github.sakc.keycloak.mcp.domain.observability.AuditService;
import io.github.sakc.keycloak.mcp.domain.security.SlidingWindowRateLimiter;
import io.github.sakc.keycloak.mcp.domain.security.ToolAuthorizationService;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.tracing.Tracer;
import jakarta.validation.Validation;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ToolExecutionTemplateTest {

    @Test
    void shouldWrapSuccessResponse() {
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

        var response = template.execute("create_user", "demo", true, new ValidRequest("value"), () -> Map.of("id", "123"));

        assertTrue(response.success());
        assertTrue(response.error() == null);
        assertTrue(response.data() instanceof Map<?, ?>);
    }

    @Test
    void shouldWrapValidationFailures() {
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

        assertThrows(io.github.sakc.keycloak.mcp.domain.error.ToolValidationException.class,
            () -> template.execute("create_user", "demo", true, new ValidRequest(""), Map::of));
    }

    record ValidRequest(@NotBlank String value) { }
}
