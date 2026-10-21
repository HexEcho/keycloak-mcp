package io.github.sakc.keycloak.mcp.api;

import java.util.List;

import io.github.sakc.keycloak.mcp.tool.KeycloakToolHandlerProvider;
import io.github.sakc.keycloak.mcp.tool.SimpleKeycloakToolHandler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ToolCatalogControllerTest {

    @Test
    void shouldReturnSortedCatalogEntries() {
        KeycloakToolHandlerProvider provider = () -> List.of(
            new SimpleKeycloakToolHandler<>("z_tool", "Z tool", VoidRequest.class, (request, context) -> "ok"),
            new SimpleKeycloakToolHandler<>("a_tool", "A tool", VoidRequest.class, (request, context) -> "ok")
        );

        ToolCatalogController controller = new ToolCatalogController(List.of(provider));
        List<ToolCatalogEntry> entries = controller.catalog();

        assertEquals(2, entries.size());
        assertEquals("a_tool", entries.getFirst().name());
        assertEquals("z_tool", entries.getLast().name());
    }

    record VoidRequest() { }
}
