package io.github.sakc.keycloak.mcp.domain.dto.user;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotEmpty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ExecuteActionsEmailRequest(
    @NotEmpty List<String> actions,
    Integer lifespan,
    String redirectUri,
    String clientId
) {
}
