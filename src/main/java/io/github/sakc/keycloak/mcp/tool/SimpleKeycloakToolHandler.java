package io.github.sakc.keycloak.mcp.tool;

import java.util.Objects;
import java.util.function.BiFunction;

import org.springframework.ai.chat.model.ToolContext;

public final class SimpleKeycloakToolHandler<I> implements KeycloakToolHandler<I> {

    private final String name;
    private final String description;
    private final Class<I> inputType;
    private final BiFunction<I, ToolContext, Object> function;

    public SimpleKeycloakToolHandler(String name, String description, Class<I> inputType, BiFunction<I, ToolContext, Object> function) {
        this.name = Objects.requireNonNull(name, "name");
        this.description = Objects.requireNonNull(description, "description");
        this.inputType = Objects.requireNonNull(inputType, "inputType");
        this.function = Objects.requireNonNull(function, "function");
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public Class<I> inputType() {
        return inputType;
    }

    @Override
    public Object handle(I input, ToolContext context) {
        return function.apply(input, context);
    }
}
