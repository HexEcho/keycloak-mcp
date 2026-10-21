package io.github.sakc.keycloak.mcp.config;

import java.time.Duration;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mcp.streamable-http")
public record StreamableHttpEnterpriseProperties(
    boolean enabled,
    String endpoint,
    Duration keepAliveInterval,
    boolean disallowDelete,
    List<String> allowedOrigins,
    List<String> allowedHosts,
    boolean requireMtls,
    String mtlsHeader
) {

    public StreamableHttpEnterpriseProperties {
        endpoint = endpoint == null || endpoint.isBlank() ? "/mcp" : endpoint;
        keepAliveInterval = keepAliveInterval == null ? Duration.ofSeconds(20) : keepAliveInterval;
        allowedOrigins = allowedOrigins == null ? List.of() : List.copyOf(allowedOrigins);
        allowedHosts = allowedHosts == null ? List.of() : List.copyOf(allowedHosts);
        mtlsHeader = mtlsHeader == null || mtlsHeader.isBlank() ? "X-Forwarded-Client-Cert" : mtlsHeader;
    }
}
