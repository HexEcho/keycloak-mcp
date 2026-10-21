package io.github.sakc.keycloak.mcp.domain.observability;

import java.util.ArrayList;
import java.util.List;

import net.logstash.logback.argument.StructuredArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private static final Logger AUDIT_LOG = LoggerFactory.getLogger("AUDIT");

    public void record(String operation, boolean mutating, String outcome, long durationMs, String code, String errorType) {
        var fields = new ArrayList<Object>();
        fields.add(StructuredArguments.keyValue("event.kind", "event"));
        fields.add(StructuredArguments.keyValue("event.category", List.of("iam")));
        fields.add(StructuredArguments.keyValue("event.type", List.of(mutating ? "change" : "access")));
        fields.add(StructuredArguments.keyValue("event.action", operation));
        fields.add(StructuredArguments.keyValue("event.outcome", outcome));
        fields.add(StructuredArguments.keyValue("event.duration", durationMs * 1_000_000L));
        if (code != null) {
            fields.add(StructuredArguments.keyValue("event.code", code));
        }
        if (errorType != null) {
            fields.add(StructuredArguments.keyValue("error.type", errorType));
        }

        AUDIT_LOG.info(outcome.equals("success") ? "Tool invocation completed" : "Tool invocation failed",
            fields.toArray());
    }
}
