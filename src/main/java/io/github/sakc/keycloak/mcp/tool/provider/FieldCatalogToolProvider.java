package io.github.sakc.keycloak.mcp.tool.provider;

import java.util.Collection;
import java.util.List;

import io.github.sakc.keycloak.mcp.domain.service.ResponseFieldCatalog;
import io.github.sakc.keycloak.mcp.domain.service.ToolExecutionTemplate;
import io.github.sakc.keycloak.mcp.tool.KeycloakToolHandler;
import io.github.sakc.keycloak.mcp.tool.KeycloakToolHandlerProvider;
import io.github.sakc.keycloak.mcp.tool.SimpleKeycloakToolHandler;

import org.springframework.stereotype.Component;

@Component
public class FieldCatalogToolProvider implements KeycloakToolHandlerProvider {

    private final List<KeycloakToolHandler<?>> handlers;

    public FieldCatalogToolProvider(ResponseFieldCatalog catalog, ToolExecutionTemplate executionTemplate) {
        this.handlers = List.of(
            new SimpleKeycloakToolHandler<>("describe_response_fields",
                "List the default and selectable response fields of read tools (optionally for one tool). Use the result to choose the 'fields' argument; ['*'] returns everything.",
                DescribeFieldsRequest.class,
                (request, context) -> executionTemplate.execute("describe_response_fields", null, false, request,
                    () -> catalog.describe(request.tool())))
        );
    }

    @Override
    public Collection<? extends KeycloakToolHandler<?>> handlers() {
        return handlers;
    }

    public record DescribeFieldsRequest(String tool) { }
}
