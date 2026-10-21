package io.github.sakc.keycloak.mcp.domain.dto.client;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ClientRepresentation(
    String id,
    String clientId,
    String name,
    String description,
    String protocol,
    String rootUrl,
    String baseUrl,
    Boolean enabled,
    Boolean publicClient,
    Boolean bearerOnly,
    Boolean serviceAccountsEnabled,
    Boolean directAccessGrantsEnabled,
    List<String> redirectUris,
    List<String> webOrigins,
    Map<String, String> attributes
) {
}
