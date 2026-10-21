package io.github.sakc.keycloak.mcp.domain.dto.client;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProtocolMapperRepresentation(
    String id,
    String name,
    String protocol,
    String protocolMapper,
    Boolean consentRequired,
    String consentText,
    Map<String, String> config
) {
}
