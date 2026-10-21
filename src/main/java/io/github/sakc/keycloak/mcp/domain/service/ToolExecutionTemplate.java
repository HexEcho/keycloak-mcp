package io.github.sakc.keycloak.mcp.domain.service;

import java.time.Instant;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import io.github.sakc.keycloak.mcp.api.ToolError;
import io.github.sakc.keycloak.mcp.api.ToolResponse;
import io.github.sakc.keycloak.mcp.domain.error.KeycloakAdminException;
import io.github.sakc.keycloak.mcp.domain.error.ToolValidationException;
import io.github.sakc.keycloak.mcp.domain.observability.AuditService;
import io.github.sakc.keycloak.mcp.domain.security.SlidingWindowRateLimiter;
import io.github.sakc.keycloak.mcp.domain.security.ToolAuthorizationService;
import io.github.sakc.keycloak.mcp.domain.security.ToolInvocationContext;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.tracing.Tracer;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ToolExecutionTemplate {

    private final Validator validator;
    private final ToolAuthorizationService authorizationService;
    private final SlidingWindowRateLimiter rateLimiter;
    private final AuditService auditService;
    private final MeterRegistry meterRegistry;
    private final Tracer tracer;

    @Autowired
    public ToolExecutionTemplate(
        Validator validator,
        ToolAuthorizationService authorizationService,
        SlidingWindowRateLimiter rateLimiter,
        AuditService auditService,
        MeterRegistry meterRegistry,
        ObjectProvider<Tracer> tracerProvider
    ) {
        this.validator = validator;
        this.authorizationService = authorizationService;
        this.rateLimiter = rateLimiter;
        this.auditService = auditService;
        this.meterRegistry = meterRegistry;
        this.tracer = tracerProvider.getIfAvailable();
    }

    public ToolExecutionTemplate(
        Validator validator,
        ToolAuthorizationService authorizationService,
        SlidingWindowRateLimiter rateLimiter,
        AuditService auditService,
        MeterRegistry meterRegistry,
        Tracer tracer
    ) {
        this.validator = validator;
        this.authorizationService = authorizationService;
        this.rateLimiter = rateLimiter;
        this.auditService = auditService;
        this.meterRegistry = meterRegistry;
        this.tracer = tracer;
    }

    public ToolResponse execute(String toolName, String realm, boolean mutating, Object request, Supplier<Object> supplier) {
        long started = System.nanoTime();
        ToolInvocationContext context;
        try {
            context = authorizationService.currentContext();
        }
        catch (RuntimeException exception) {
            recordUnexpectedFailure(toolName, mutating, durationMs(started), exception);
            throw exception;
        }

        try {
            rateLimiter.acquire(context.principal(), toolName, context.correlationId());
            authorizationService.authorize(toolName, mutating, context);
            validate(request, context.correlationId());
        }
        catch (KeycloakAdminException exception) {
            long durationMs = durationMs(started);
            recordFailure(toolName, mutating, durationMs, exception);
            throw exception;
        }
        catch (RuntimeException exception) {
            recordUnexpectedFailure(toolName, mutating, durationMs(started), exception);
            throw exception;
        }

        var span = tracer == null ? null : tracer.nextSpan().name("mcp.tool." + toolName).tag("realm", realm == null ? "global" : realm).start();
        try (Tracer.SpanInScope ignored = tracer == null ? null : tracer.withSpan(span)) {
            Object data = supplier.get();
            long durationMs = durationMs(started);
            meterRegistry.counter("mcp.tool.invocations", "tool", toolName, "outcome", "success").increment();
            recordLatency(toolName, durationMs);
            auditService.record(toolName, mutating, "success", durationMs, null, null);
            return ToolResponse.success(toolName, realm, context.correlationId(), context.traceId(), durationMs, data);
        }
        catch (KeycloakAdminException exception) {
            long durationMs = durationMs(started);
            recordFailure(toolName, mutating, durationMs, exception);
            return ToolResponse.failure(toolName, realm, context.correlationId(), context.traceId(), durationMs, new ToolError(
                exception.code(),
                exception.getMessage(),
                exception.httpStatus(),
                exception.keycloakError(),
                exception.requestId(),
                exception.correlationId(),
                Instant.now()
            ));
        }
        catch (RuntimeException exception) {
            recordUnexpectedFailure(toolName, mutating, durationMs(started), exception);
            throw exception;
        }
        finally {
            if (span != null) {
                span.end();
            }
        }
    }

    private void recordFailure(String toolName, boolean mutating, long durationMs, KeycloakAdminException exception) {
        meterRegistry.counter("mcp.tool.invocations", "tool", toolName, "outcome", "error", "code", exception.code()).increment();
        recordLatency(toolName, durationMs);
        auditService.record(toolName, mutating, "failure", durationMs, exception.code(), exception.getClass().getName());
    }

    private void recordUnexpectedFailure(String toolName, boolean mutating, long durationMs, RuntimeException exception) {
        meterRegistry.counter("mcp.tool.invocations", "tool", toolName, "outcome", "error", "code", "internal_error").increment();
        recordLatency(toolName, durationMs);
        auditService.record(toolName, mutating, "failure", durationMs, "internal_error", exception.getClass().getName());
    }

    private void recordLatency(String toolName, long durationMs) {
        Timer.builder("mcp.tool.latency").tag("tool", toolName).register(meterRegistry)
            .record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    private void validate(Object request, String correlationId) {
        if (request == null) {
            return;
        }
        Set<ConstraintViolation<Object>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
                .collect(Collectors.joining(", "));
            throw new ToolValidationException(message, correlationId);
        }
    }

    private long durationMs(long started) {
        return (System.nanoTime() - started) / 1_000_000;
    }
}
