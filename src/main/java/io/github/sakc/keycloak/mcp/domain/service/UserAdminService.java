package io.github.sakc.keycloak.mcp.domain.service;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.sakc.keycloak.mcp.domain.client.KeycloakAdminClient;
import io.github.sakc.keycloak.mcp.domain.dto.user.CredentialRepresentation;
import io.github.sakc.keycloak.mcp.domain.dto.user.ExecuteActionsEmailRequest;
import io.github.sakc.keycloak.mcp.domain.dto.user.RoleMappingRepresentation;
import io.github.sakc.keycloak.mcp.domain.dto.user.UserRepresentation;

import org.springframework.stereotype.Service;

@Service
public class UserAdminService extends AbstractKeycloakDomainService {

    public UserAdminService(KeycloakAdminClient keycloakAdminClient, KeycloakPathBuilder paths) {
        super(keycloakAdminClient, paths);
    }

    public Map<String, Object> createUser(String realm, UserRepresentation payload) {
        var response = postResponse(paths.users(realm), payload);
        return Map.of("userId", locationId(response.headers()), "status", response.status(), "realm", realm);
    }

    public Map<String, Object> updateUser(String realm, String userId, UserRepresentation payload) {
        var response = putResponse(paths.user(realm, userId), payload);
        return actionResult("updateUser", response.status(), response.body());
    }

    public Map<String, Object> deleteUser(String realm, String userId) {
        var response = deleteResponse(paths.user(realm, userId));
        return actionResult("deleteUser", response.status(), response.body());
    }

    public JsonNode searchUsers(String realm, Map<String, Object> query) {
        return get(paths.users(realm), query);
    }

    public JsonNode getUser(String realm, String userId) {
        return get(paths.user(realm, userId));
    }

    public Map<String, Object> resetPassword(String realm, String userId, CredentialRepresentation passwordCredential) {
        var response = putResponse(paths.user(realm, userId) + "/reset-password", passwordCredential);
        return actionResult("resetPassword", response.status(), response.body());
    }

    public Map<String, Object> setEnabled(String realm, String userId, boolean enabled) {
        return updateUser(realm, userId, new UserRepresentation(null, null, null, null, null, enabled, null, null, null, null, null));
    }

    public Map<String, Object> executeActions(String realm, String userId, List<String> actions, Integer lifespan, String redirectUri, String clientId) {
        var payload = new ExecuteActionsEmailRequest(actions, lifespan, redirectUri, clientId);
        var response = putResponse(paths.user(realm, userId) + "/execute-actions-email", payload);
        return actionResult("executeActions", response.status(), response.body());
    }

    public Map<String, Object> logoutSessions(String realm, String userId) {
        var response = postResponse(paths.user(realm, userId) + "/logout", Map.of());
        return actionResult("logoutSessions", response.status(), response.body());
    }

    public Map<String, Object> updateAttributes(String realm, String userId, Map<String, List<String>> attributes) {
        return updateUser(realm, userId, new UserRepresentation(null, null, null, null, null, null, null, attributes, null, null, null));
    }

    public Map<String, Object> assignRealmRoles(String realm, String userId, List<RoleMappingRepresentation> roles) {
        var response = postResponse(paths.user(realm, userId) + "/role-mappings/realm", roles);
        return actionResult("assignRealmRoles", response.status(), response.body());
    }

    public Map<String, Object> removeRealmRoles(String realm, String userId, List<RoleMappingRepresentation> roles) {
        var response = keycloakAdminClient.exchange(org.springframework.http.HttpMethod.DELETE, paths.user(realm, userId) + "/role-mappings/realm", null, roles);
        return actionResult("removeRealmRoles", response.status(), response.body());
    }

    public Map<String, Object> assignClientRoles(String realm, String userId, String clientUuid, List<RoleMappingRepresentation> roles) {
        var response = postResponse(paths.user(realm, userId) + "/role-mappings/clients/" + clientUuid, roles);
        return actionResult("assignClientRoles", response.status(), response.body());
    }

    public Map<String, Object> removeClientRoles(String realm, String userId, String clientUuid, List<RoleMappingRepresentation> roles) {
        var response = keycloakAdminClient.exchange(org.springframework.http.HttpMethod.DELETE, paths.user(realm, userId) + "/role-mappings/clients/" + clientUuid, null, roles);
        return actionResult("removeClientRoles", response.status(), response.body());
    }

    public Map<String, Object> joinGroups(String realm, String userId, List<String> groupIds) {
        List<Map<String, Object>> results = groupIds.stream().<Map<String, Object>>map(groupId -> {
            var response = putResponse(paths.user(realm, userId) + "/groups/" + groupId, Map.of());
            return Map.of("groupId", groupId, "status", response.status());
        }).toList();
        return Map.of("realm", realm, "userId", userId, "results", results);
    }

    public Map<String, Object> leaveGroups(String realm, String userId, List<String> groupIds) {
        List<Map<String, Object>> results = groupIds.stream().<Map<String, Object>>map(groupId -> {
            var response = deleteResponse(paths.user(realm, userId) + "/groups/" + groupId);
            return Map.of("groupId", groupId, "status", response.status());
        }).toList();
        return Map.of("realm", realm, "userId", userId, "results", results);
    }

    public JsonNode listCredentials(String realm, String userId) {
        return get(paths.user(realm, userId) + "/credentials");
    }

    public JsonNode listFederatedIdentities(String realm, String userId) {
        return get(paths.user(realm, userId) + "/federated-identity");
    }

    public Map<String, Object> unlockUser(String realm, String userId) {
        var response = deleteResponse(paths.user(realm, userId) + "/brute-force-user");
        return actionResult("unlockUser", response.status(), response.body());
    }
}
