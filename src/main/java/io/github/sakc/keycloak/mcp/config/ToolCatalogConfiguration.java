package io.github.sakc.keycloak.mcp.config;

import java.util.List;
import java.util.Map;

import io.github.sakc.keycloak.mcp.api.ToolResponse;
import io.github.sakc.keycloak.mcp.domain.service.ResponseFieldCatalog;
import io.github.sakc.keycloak.mcp.tool.KeycloakToolHandler;
import io.github.sakc.keycloak.mcp.tool.KeycloakToolHandlerProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.metadata.ToolMetadata;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ToolCatalogConfiguration {

    private final ObjectMapper objectMapper;

    private final ResponseFieldCatalog catalog;

    public ToolCatalogConfiguration(ObjectMapper objectMapper, ResponseFieldCatalog catalog) {
        this.objectMapper = objectMapper;
        this.catalog = catalog;
    }

    @Bean
    public ToolCallbackProvider keycloakToolCallbackProvider(List<KeycloakToolHandlerProvider> providers) {
        List<ToolCallback> callbacks = providers.stream()
            .flatMap(provider -> provider.handlers().stream())
            .map(this::toToolCallback)
            .toList();

        return () -> callbacks.toArray(ToolCallback[]::new);
    }

    private <I> ToolCallback toToolCallback(KeycloakToolHandler<I> handler) {
        ToolDefinition definition = ToolDefinition.builder()
            .name(handler.name())
            .description(handler.description())
            .inputSchema("{\"type\":\"object\",\"additionalProperties\":true}")
            .build();
        return new KeycloakToolCallback<>(handler, definition, objectMapper, new ResponseProjector(objectMapper, catalog));
    }

    private <I> I convertInput(Map<String, Object> input, Class<I> inputType) {
        if (Void.class.equals(inputType)) {
            return null;
        }
        return objectMapper.convertValue(input, inputType);
    }

    private static final class KeycloakToolCallback<I> implements ToolCallback {

        private final KeycloakToolHandler<I> handler;
        private final ToolDefinition definition;
        private final ToolMetadata metadata;
        private final ObjectMapper objectMapper;
        private final ResponseProjector projector;

        private KeycloakToolCallback(KeycloakToolHandler<I> handler, ToolDefinition definition, ObjectMapper objectMapper, ResponseProjector projector) {
            this.projector = projector;
            this.handler = handler;
            this.definition = definition;
            this.objectMapper = objectMapper;
            this.metadata = ToolMetadata.builder().returnDirect(false).build();
        }

        @Override
        public ToolDefinition getToolDefinition() {
            return definition;
        }

        @Override
        public ToolMetadata getToolMetadata() {
            return metadata;
        }

        @Override
        public String call(String input) {
            return call(input, new ToolContext(Map.of()));
        }

        @Override
        public String call(String input, ToolContext context) {
            try {
                I typedInput = parseInput(input);
                Object result = handler.handle(typedInput, context);
                result = project(result, input);
                return result == null ? "null" : objectMapper.writeValueAsString(result);
            }
            catch (Exception exception) {
                String message = exception.getMessage() == null ? "unknown error" : exception.getMessage();
                throw new IllegalArgumentException("Tool execution failed for " + handler.name() + ": " + message, exception);
            }
        }

        private Object project(Object result, String input) throws com.fasterxml.jackson.core.JsonProcessingException {
            if (!projector.supports(handler.name()) || !(result instanceof ToolResponse response)
                || !response.success() || response.data() == null) {
                return result;
            }
            JsonNode arguments = input == null || input.isBlank() ? null : objectMapper.readTree(input);
            var fields = projector.resolveFields(handler.name(), arguments);
            return new ToolResponse(response.success(), response.operation(), response.realm(), response.correlationId(),
                response.traceId(), response.durationMs(), response.timestamp(), projector.project(response.data(), fields), response.error());
        }

        private I parseInput(String input) throws com.fasterxml.jackson.core.JsonProcessingException {
            if (Void.class.equals(handler.inputType())) {
                return null;
            }
            if (input == null || input.isBlank()) {
                return objectMapper.convertValue(Map.of(), handler.inputType());
            }
            return objectMapper.readValue(input, handler.inputType());
        }
    }
}
