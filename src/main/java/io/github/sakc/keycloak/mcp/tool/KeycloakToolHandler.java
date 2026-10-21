package io.github.sakc.keycloak.mcp.tool;

import org.springframework.ai.chat.model.ToolContext;

public interface KeycloakToolHandler<I> {

    String name();

    String description();

    Class<I> inputType();

    Object handle(I input, ToolContext context);
}
