package io.github.sakc.keycloak.mcp.domain.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RoleMappingRepresentation(
    String id,
    @NotBlank String name,
    String description,
    Boolean composite,
    String clientRole,
    String containerId
) {
}
