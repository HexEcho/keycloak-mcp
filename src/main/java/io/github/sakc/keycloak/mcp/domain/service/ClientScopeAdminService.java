package io.github.sakc.keycloak.mcp.domain.service;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.sakc.keycloak.mcp.domain.client.KeycloakAdminClient;

import org.springframework.stereotype.Service;

@Service
public class ClientScopeAdminService extends AbstractKeycloakDomainService {

    public ClientScopeAdminService(KeycloakAdminClient keycloakAdminClient, KeycloakPathBuilder paths) {
        super(keycloakAdminClient, paths);
    }

    public JsonNode listClientScopes(String realm) {
        return get(paths.clientScopes(realm));
    }

    public JsonNode getClientScope(String realm, String scopeId) {
        return get(paths.clientScope(realm, scopeId));
    }

    public Map<String, Object> createClientScope(String realm, Map<String, Object> payload) {
        var response = postResponse(paths.clientScopes(realm), payload);
        return Map.of("scopeId", locationId(response.headers()), "status", response.status());
    }

    public Map<String, Object> updateClientScope(String realm, String scopeId, Map<String, Object> payload) {
        var response = putResponse(paths.clientScope(realm, scopeId), payload);
        return actionResult("updateClientScope", response.status(), response.body());
    }

    public Map<String, Object> deleteClientScope(String realm, String scopeId) {
        var response = deleteResponse(paths.clientScope(realm, scopeId));
        return actionResult("deleteClientScope", response.status(), response.body());
    }

    public JsonNode listProtocolMappers(String realm, String scopeId) {
        return get(paths.clientScope(realm, scopeId) + "/protocol-mappers/models");
    }

    public Map<String, Object> createProtocolMapper(String realm, String scopeId, Map<String, Object> payload) {
        var response = postResponse(paths.clientScope(realm, scopeId) + "/protocol-mappers/models", payload);
        return actionResult("createClientScopeMapper", response.status(), response.body());
    }

    public Map<String, Object> attachDefaultScopes(String realm, List<String> scopeIds) {
        List<Map<String, Object>> results = scopeIds.stream().<Map<String, Object>>map(scopeId -> {
            var response = putResponse(paths.realm(realm) + "/default-default-client-scopes/" + scopeId, Map.of());
            return Map.of("scopeId", scopeId, "status", response.status());
        }).toList();
        return Map.of("realm", realm, "results", results);
    }

    public Map<String, Object> attachOptionalScopes(String realm, List<String> scopeIds) {
        List<Map<String, Object>> results = scopeIds.stream().<Map<String, Object>>map(scopeId -> {
            var response = putResponse(paths.realm(realm) + "/default-optional-client-scopes/" + scopeId, Map.of());
            return Map.of("scopeId", scopeId, "status", response.status());
        }).toList();
        return Map.of("realm", realm, "results", results);
    }
}
