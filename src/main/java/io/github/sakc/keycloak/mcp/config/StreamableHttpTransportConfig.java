package io.github.sakc.keycloak.mcp.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.server.transport.DefaultServerTransportSecurityValidator;
import io.modelcontextprotocol.server.transport.ServerTransportSecurityException;
import io.modelcontextprotocol.server.transport.ServerTransportSecurityValidator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.ServerRequest;

@Configuration
@EnableConfigurationProperties(StreamableHttpEnterpriseProperties.class)
@ConditionalOnProperty(prefix = "mcp.streamable-http", name = "enabled", havingValue = "true", matchIfMissing = true)
public class StreamableHttpTransportConfig {

    @Bean
    ServerTransportSecurityValidator streamableHttpSecurityValidator(StreamableHttpEnterpriseProperties properties) {
        DefaultServerTransportSecurityValidator.Builder builder = DefaultServerTransportSecurityValidator.builder();
        if (!properties.allowedOrigins().isEmpty()) {
            builder.allowedOrigins(properties.allowedOrigins());
        }
        if (!properties.allowedHosts().isEmpty()) {
            builder.allowedHosts(properties.allowedHosts());
        }
        DefaultServerTransportSecurityValidator delegate = builder.build();

        return headers -> {
            delegate.validateHeaders(headers);
            if (properties.requireMtls()) {
                List<String> certificateHeader = findHeaderIgnoreCase(headers, properties.mtlsHeader());
                if (certificateHeader.isEmpty() || certificateHeader.getFirst().isBlank()) {
                    throw new ServerTransportSecurityException(401,
                        "Mutual TLS is required but the forwarded client certificate header is missing");
                }
            }
        };
    }

    private List<String> findHeaderIgnoreCase(Map<String, List<String>> headers, String name) {
        return headers.entrySet().stream()
            .filter(entry -> entry.getKey().equalsIgnoreCase(name))
            .map(Map.Entry::getValue)
            .findFirst()
            .orElse(List.of());
    }
}
