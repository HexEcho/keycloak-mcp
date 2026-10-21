package io.github.sakc.keycloak.mcp.domain.observability;

import java.util.Map;

import io.github.sakc.keycloak.mcp.domain.security.ToolInvocationContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private static final Logger AUDIT_LOG = LoggerFactory.getLogger("AUDIT");

    public void record(String operation, String realm, boolean mutating, ToolInvocationContext context, Map<String, Object> details) {
        if (!mutating) {
            return;
        }
        AUDIT_LOG.info("audit operation={} realm={} principal={} correlationId={} traceId={} details={}",
            operation,
            realm,
            context.principal(),
            context.correlationId(),
            context.traceId(),
            details);
    }
}
