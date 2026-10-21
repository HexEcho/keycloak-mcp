package io.github.sakc.keycloak.mcp.domain.client;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.sakc.keycloak.mcp.domain.error.AuthenticationException;
import io.github.sakc.keycloak.mcp.domain.error.AuthorizationException;
import io.github.sakc.keycloak.mcp.domain.error.ConflictException;
import io.github.sakc.keycloak.mcp.domain.error.KeycloakAdminException;
import io.github.sakc.keycloak.mcp.domain.error.KeycloakServerException;
import io.github.sakc.keycloak.mcp.domain.error.RealmNotFoundException;
import io.github.sakc.keycloak.mcp.domain.error.UserNotFoundException;

import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;

@Component
public class KeycloakExceptionMapper {

    public KeycloakAdminException map(HttpStatusCode status, JsonNode body, String path, String requestId, String correlationId) {
        String message = extractMessage(body, status.value());
        String keycloakError = body == null ? null : body.path("error").asText(null);
        return switch (status.value()) {
            case 401 -> new AuthenticationException(message, 401, keycloakError, requestId, correlationId);
            case 403 -> new AuthorizationException(message, 403, keycloakError, requestId, correlationId);
            case 404 -> mapNotFound(message, path, requestId, correlationId);
            case 409 -> new ConflictException(message, 409, keycloakError, requestId, correlationId);
            default -> {
                if (status.is5xxServerError()) {
                    yield new KeycloakServerException(message, status.value(), keycloakError, requestId, correlationId);
                }
                yield new KeycloakAdminException("keycloak_error", message, status.value(), keycloakError, requestId, correlationId);
            }
        };
    }

    private KeycloakAdminException mapNotFound(String message, String path, String requestId, String correlationId) {
        if (path.contains("/users/")) {
            return new UserNotFoundException(message, requestId, correlationId);
        }
        if (path.contains("/realms/")) {
            return new RealmNotFoundException(message, requestId, correlationId);
        }
        return new KeycloakAdminException("not_found", message, 404, null, requestId, correlationId);
    }

    private String extractMessage(JsonNode body, int httpStatus) {
        if (body == null || body.isMissingNode() || body.isNull()) {
            return "Keycloak request failed with status " + httpStatus;
        }
        if (body.path("error_description").isTextual()) {
            return body.path("error_description").asText();
        }
        if (body.path("errorMessage").isTextual()) {
            return body.path("errorMessage").asText();
        }
        if (body.path("message").isTextual()) {
            return body.path("message").asText();
        }
        if (body.path("error").isTextual()) {
            return body.path("error").asText();
        }
        return body.toString();
    }
}
