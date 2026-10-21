package io.github.sakc.keycloak.mcp.domain.dto.user;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserRepresentation(
    String id,
    String username,
    String firstName,
    String lastName,
    String email,
    Boolean enabled,
    Boolean emailVerified,
    Map<String, List<String>> attributes,
    List<CredentialRepresentation> credentials,
    List<String> requiredActions,
    Long createdTimestamp
) {
}
