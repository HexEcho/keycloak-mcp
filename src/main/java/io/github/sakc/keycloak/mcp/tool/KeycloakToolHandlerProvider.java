package io.github.sakc.keycloak.mcp.tool;

import java.util.Collection;

public interface KeycloakToolHandlerProvider {

    Collection<? extends KeycloakToolHandler<?>> handlers();
}
