package io.github.sakc.keycloak.mcp.domain.service;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.sakc.keycloak.mcp.domain.client.KeycloakAdminClient;

import org.springframework.stereotype.Service;

@Service
public class RoleAdminService extends AbstractKeycloakDomainService {

    public RoleAdminService(KeycloakAdminClient keycloakAdminClient, KeycloakPathBuilder paths) {
        super(keycloakAdminClient, paths);
    }

    public JsonNode listRealmRoles(String realm) {
        return get(paths.roles(realm));
    }

    public Map<String, Object> createRealmRole(String realm, Map<String, Object> payload) {
        var response = postResponse(paths.roles(realm), payload);
        return actionResult("createRealmRole", response.status(), response.body());
    }

    public JsonNode getRealmRole(String realm, String roleName) {
        return get(paths.role(realm, roleName));
    }

    public Map<String, Object> updateRealmRole(String realm, String roleName, Map<String, Object> payload) {
        var response = putResponse(paths.role(realm, roleName), payload);
        return actionResult("updateRealmRole", response.status(), response.body());
    }

    public Map<String, Object> deleteRealmRole(String realm, String roleName) {
        var response = deleteResponse(paths.role(realm, roleName));
        return actionResult("deleteRealmRole", response.status(), response.body());
    }

    public JsonNode listClientRoles(String realm, String clientUuid) {
        return get(paths.clientRoles(realm, clientUuid));
    }

    public Map<String, Object> createClientRole(String realm, String clientUuid, Map<String, Object> payload) {
        var response = postResponse(paths.clientRoles(realm, clientUuid), payload);
        return actionResult("createClientRole", response.status(), response.body());
    }

    public Map<String, Object> updateClientRole(String realm, String clientUuid, String roleName, Map<String, Object> payload) {
        var response = putResponse(paths.clientRole(realm, clientUuid, roleName), payload);
        return actionResult("updateClientRole", response.status(), response.body());
    }

    public Map<String, Object> assignCompositeRoles(String realm, String roleName, List<Map<String, Object>> roles) {
        var response = postResponse(paths.role(realm, roleName) + "/composites", roles);
        return actionResult("assignCompositeRoles", response.status(), response.body());
    }

    public Map<String, Object> removeCompositeRoles(String realm, String roleName, List<Map<String, Object>> roles) {
        var response = keycloakAdminClient.exchange(org.springframework.http.HttpMethod.DELETE, paths.role(realm, roleName) + "/composites", null, roles);
        return actionResult("removeCompositeRoles", response.status(), response.body());
    }

    public JsonNode usersWithRole(String realm, String roleName) {
        return get(paths.role(realm, roleName) + "/users");
    }

    public JsonNode groupsWithRole(String realm, String roleName) {
        return get(paths.role(realm, roleName) + "/groups");
    }
}
