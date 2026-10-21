package io.github.sakc.keycloak.mcp.domain.service;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.sakc.keycloak.mcp.domain.client.KeycloakAdminClient;
import io.github.sakc.keycloak.mcp.domain.dto.client.ClientRepresentation;
import io.github.sakc.keycloak.mcp.domain.dto.client.ProtocolMapperRepresentation;

import org.springframework.stereotype.Service;

@Service
public class ClientAdminService extends AbstractKeycloakDomainService {

    public ClientAdminService(KeycloakAdminClient keycloakAdminClient, KeycloakPathBuilder paths) {
        super(keycloakAdminClient, paths);
    }

    public Map<String, Object> createClient(String realm, ClientRepresentation payload) {
        var response = postResponse(paths.clients(realm), payload);
        return Map.of("clientUuid", locationId(response.headers()), "status", response.status(), "realm", realm);
    }

    public Map<String, Object> updateClient(String realm, String clientUuid, ClientRepresentation payload) {
        var response = putResponse(paths.client(realm, clientUuid), payload);
        return actionResult("updateClient", response.status(), response.body());
    }

    public Map<String, Object> deleteClient(String realm, String clientUuid) {
        var response = deleteResponse(paths.client(realm, clientUuid));
        return actionResult("deleteClient", response.status(), response.body());
    }

    public JsonNode listClients(String realm, Map<String, Object> query) {
        return get(paths.clients(realm), query);
    }

    public JsonNode getClient(String realm, String clientUuid) {
        return get(paths.client(realm, clientUuid));
    }

    public Map<String, Object> rotateSecret(String realm, String clientUuid) {
        var response = postResponse(paths.client(realm, clientUuid) + "/client-secret", Map.of());
        return Map.of("status", response.status(), "secret", response.body());
    }

    public JsonNode listProtocolMappers(String realm, String clientUuid) {
        return get(paths.client(realm, clientUuid) + "/protocol-mappers/models");
    }

    public Map<String, Object> createProtocolMapper(String realm, String clientUuid, ProtocolMapperRepresentation mapper) {
        var response = postResponse(paths.client(realm, clientUuid) + "/protocol-mappers/models", mapper);
        return actionResult("createProtocolMapper", response.status(), response.body());
    }

    public Map<String, Object> updateProtocolMapper(String realm, String clientUuid, String mapperId, ProtocolMapperRepresentation mapper) {
        var response = putResponse(paths.client(realm, clientUuid) + "/protocol-mappers/models/" + mapperId, mapper);
        return actionResult("updateProtocolMapper", response.status(), response.body());
    }

    public Map<String, Object> deleteProtocolMapper(String realm, String clientUuid, String mapperId) {
        var response = deleteResponse(paths.client(realm, clientUuid) + "/protocol-mappers/models/" + mapperId);
        return actionResult("deleteProtocolMapper", response.status(), response.body());
    }

    public Map<String, Object> attachDefaultClientScopes(String realm, String clientUuid, List<String> scopeIds) {
        List<Map<String, Object>> results = scopeIds.stream().<Map<String, Object>>map(scopeId -> {
            var response = putResponse(paths.client(realm, clientUuid) + "/default-client-scopes/" + scopeId, Map.of());
            return Map.of("scopeId", scopeId, "status", response.status());
        }).toList();
        return Map.of("results", results, "realm", realm, "clientUuid", clientUuid);
    }
}
