package io.github.sakc.keycloak.mcp.domain.service;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.sakc.keycloak.mcp.domain.client.KeycloakAdminClient;
import io.github.sakc.keycloak.mcp.domain.client.KeycloakResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Component
public abstract class AbstractKeycloakDomainService {

    protected final KeycloakAdminClient keycloakAdminClient;
    protected final KeycloakPathBuilder paths;

    protected AbstractKeycloakDomainService(KeycloakAdminClient keycloakAdminClient, KeycloakPathBuilder paths) {
        this.keycloakAdminClient = keycloakAdminClient;
        this.paths = paths;
    }

    protected JsonNode get(String path) {
        return keycloakAdminClient.get(path, null).body();
    }

    protected JsonNode get(String path, Map<String, ?> query) {
        return keycloakAdminClient.get(path, toQuery(query)).body();
    }

    protected JsonNode post(String path, Object body) {
        return keycloakAdminClient.post(path, body).body();
    }

    protected JsonNode put(String path, Object body) {
        return keycloakAdminClient.put(path, body).body();
    }

    protected JsonNode delete(String path) {
        return keycloakAdminClient.delete(path).body();
    }

    protected KeycloakResponse postResponse(String path, Object body) {
        return keycloakAdminClient.post(path, body);
    }

    protected KeycloakResponse putResponse(String path, Object body) {
        return keycloakAdminClient.put(path, body);
    }

    protected KeycloakResponse deleteResponse(String path) {
        return keycloakAdminClient.delete(path);
    }

    protected String locationId(HttpHeaders headers) {
        String location = headers.getFirst(HttpHeaders.LOCATION);
        if (location == null || location.isBlank()) {
            return null;
        }
        int index = location.lastIndexOf('/');
        return index >= 0 ? location.substring(index + 1) : location;
    }

    protected MultiValueMap<String, String> toQuery(Map<String, ?> query) {
        LinkedMultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        if (query == null) {
            return parameters;
        }
        query.forEach((key, value) -> {
            if (value == null) {
                return;
            }
            if (value instanceof Iterable<?> iterable) {
                for (Object item : iterable) {
                    parameters.add(key, String.valueOf(item));
                }
                return;
            }
            if (value.getClass().isArray()) {
                Object[] items = (Object[]) value;
                for (Object item : items) {
                    parameters.add(key, String.valueOf(item));
                }
                return;
            }
            parameters.add(key, String.valueOf(value));
        });
        return parameters;
    }

    protected Map<String, Object> actionResult(String operation, int status, JsonNode body) {
        return Map.of(
            "operation", operation,
            "status", status,
            "body", body == null ? List.of() : body
        );
    }
}
