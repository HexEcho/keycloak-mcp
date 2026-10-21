package io.github.sakc.keycloak.mcp.api;

import java.util.List;

import io.github.sakc.keycloak.mcp.tool.KeycloakToolHandler;
import io.github.sakc.keycloak.mcp.tool.KeycloakToolHandlerProvider;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tools")
public class ToolCatalogController {

    private final List<KeycloakToolHandlerProvider> providers;

    public ToolCatalogController(List<KeycloakToolHandlerProvider> providers) {
        this.providers = providers;
    }

    @GetMapping("/catalog")
    public List<ToolCatalogEntry> catalog() {
        return providers.stream()
            .flatMap(provider -> provider.handlers().stream())
            .map(this::toEntry)
            .sorted(java.util.Comparator.comparing(ToolCatalogEntry::name))
            .toList();
    }

    private ToolCatalogEntry toEntry(KeycloakToolHandler<?> handler) {
        return new ToolCatalogEntry(handler.name(), handler.description(), handler.inputType().getSimpleName());
    }
}
