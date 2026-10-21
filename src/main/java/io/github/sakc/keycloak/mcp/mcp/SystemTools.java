package io.github.sakc.keycloak.mcp.mcp;

import java.time.Instant;
import java.util.Map;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class SystemTools {

    @Tool(name = "server_info", description = "Returns MCP server metadata and readiness basics")
    public Map<String, Object> serverInfo() {
        return Map.of(
            "service", "keycloak-admin-mcp-server",
            "timestamp", Instant.now().toString(),
            "status", "UP"
        );
    }
}
