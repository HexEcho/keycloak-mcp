package io.github.sakc.keycloak.mcp.domain.observability;

import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import java.util.Set;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.logstash.logback.argument.StructuredArgument;
import net.logstash.logback.mask.MaskingJsonGeneratorDecorator;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuditServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldEmitSafeEcsOutcomeFieldsWithoutCallerIdentifiers() throws Exception {
        Logger logger = (Logger) LoggerFactory.getLogger("AUDIT");
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.setContext(logger.getLoggerContext());
        appender.start();
        logger.addAppender(appender);

        try {
            new AuditService().record("list_users", false, "failure", 12, "keycloak_error", "KeycloakAdminException");
        }
        finally {
            logger.detachAppender(appender);
            appender.stop();
        }

        ILoggingEvent event = appender.list.getFirst();
        assertEquals("Tool invocation failed", event.getFormattedMessage());
        ObjectNode fields = structuredFields(event);
        assertEquals("event", fields.path("event.kind").asText());
        assertEquals("iam", fields.path("event.category").get(0).asText());
        assertEquals("access", fields.path("event.type").get(0).asText());
        assertEquals("list_users", fields.path("event.action").asText());
        assertEquals("failure", fields.path("event.outcome").asText());
        assertEquals(12_000_000L, fields.path("event.duration").asLong());
        assertEquals("keycloak_error", fields.path("event.code").asText());
        assertEquals("KeycloakAdminException", fields.path("error.type").asText());
        assertEquals(Set.of("event.kind", "event.category", "event.type", "event.action", "event.outcome",
            "event.duration", "event.code", "error.type"), fieldNames(fields));
        assertFalse(event.getFormattedMessage().contains("correlationId"));
        assertFalse(event.getFormattedMessage().contains("principal"));
    }

    @Test
    void shouldMaskInlineSecretsAndSensitiveFieldNames() throws Exception {
        MaskingJsonGeneratorDecorator decorator = new MaskingJsonGeneratorDecorator();
        decorator.setDefaultMask("[REDACTED]");
        decorator.addPath("password");
        decorator.addPath("secret");
        decorator.addPath("client_secret");
        decorator.addPath("clientSecret");
        decorator.addPath("access_token");
        decorator.addPath("accessToken");
        decorator.addPath("refresh_token");
        decorator.addPath("refreshToken");
        decorator.addPath("id_token");
        decorator.addPath("idToken");
        decorator.addPath("token");
        decorator.addPath("credential");
        decorator.addPath("authorization");
        decorator.addPath("client_id");
        decorator.addPath("clientId");
        decorator.addPath("principal");
        decorator.addPath("realm");
        decorator.addPath("correlationId");
        decorator.addPath("traceId");
        decorator.addValue("(?i).*(password|secret|client_secret|access_token|refresh_token|id_token|token|credential|client_id|authorization|x-correlation-id|correlation.?id|trace.?id|principal|realm)([\"']?\\s*[:=]\\s*[\"']?|\\s+)[^\\s,\"';&]+.*");
        decorator.addValue("(?i).*Bearer\\s+[A-Za-z0-9._~+/-]+=*.*");
        decorator.addValue(".*\\beyJ[A-Za-z0-9_-]*\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\b.*");
        decorator.start();
        String fakeToken = "not-a-" + "real-token";
        String fakePassword = "example-" + "value";

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (var generator = decorator.decorate(objectMapper.getFactory().createGenerator(output))) {
            generator.writeStartObject();
            generator.writeStringField("message", "Request failed password=hunter2 X-Correlation-Id=caller-id");
            generator.writeStringField("passwordMessage", "Request failed password=" + fakePassword);
            generator.writeStringField("jsonMessage", "{\"client_secret\":\"" + fakeToken + "\"}");
            generator.writeStringField("client_id", "internal-client");
            generator.writeStringField("event.action", "rotate_client_secret");
            generator.writeEndObject();
        }
        finally {
            decorator.stop();
        }

        String encoded = output.toString(java.nio.charset.StandardCharsets.UTF_8);
        assertFalse(encoded.contains(fakePassword));
        assertFalse(encoded.contains(fakeToken));
        assertFalse(encoded.contains("hunter2"));
        assertFalse(encoded.contains("caller-id"));
        assertFalse(encoded.contains("internal-client"));
        assertTrue(encoded.contains("\"event.action\":\"rotate_client_secret\""));
    }

    private ObjectNode structuredFields(ILoggingEvent event) throws Exception {
        ObjectNode fields = objectMapper.createObjectNode();
        for (Object argument : event.getArgumentArray()) {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try (var generator = objectMapper.getFactory().createGenerator(output)) {
                generator.writeStartObject();
                ((StructuredArgument) argument).writeTo(generator);
                generator.writeEndObject();
            }
            fields.setAll((ObjectNode) objectMapper.readTree(output.toByteArray()));
        }
        return fields;
    }

    private Set<String> fieldNames(ObjectNode fields) {
        Set<String> names = new HashSet<>();
        fields.fieldNames().forEachRemaining(names::add);
        return names;
    }
}
