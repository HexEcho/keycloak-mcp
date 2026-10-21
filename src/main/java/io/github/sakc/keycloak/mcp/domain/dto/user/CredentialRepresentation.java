package io.github.sakc.keycloak.mcp.domain.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CredentialRepresentation(
    String id,
    String type,
    String value,
    Boolean temporary,
    Long createdDate,
    Integer priority,
    String userLabel
) {
}
