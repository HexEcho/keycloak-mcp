package io.github.sakc.keycloak.mcp.domain.service;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.sakc.keycloak.mcp.domain.client.KeycloakAdminClient;

import org.springframework.stereotype.Service;

@Service
public class GroupAdminService extends AbstractKeycloakDomainService {

    public GroupAdminService(KeycloakAdminClient keycloakAdminClient, KeycloakPathBuilder paths) {
        super(keycloakAdminClient, paths);
    }

    public JsonNode listGroups(String realm, Map<String, Object> query) {
        return get(paths.groups(realm), query);
    }

    public JsonNode getGroup(String realm, String groupId) {
        return get(paths.group(realm, groupId));
    }

    public Map<String, Object> createGroup(String realm, Map<String, Object> payload) {
        var response = postResponse(paths.groups(realm), payload);
        return Map.of("groupId", locationId(response.headers()), "status", response.status());
    }

    public Map<String, Object> updateGroup(String realm, String groupId, Map<String, Object> payload) {
        var response = putResponse(paths.group(realm, groupId), payload);
        return actionResult("updateGroup", response.status(), response.body());
    }

    public Map<String, Object> deleteGroup(String realm, String groupId) {
        var response = deleteResponse(paths.group(realm, groupId));
        return actionResult("deleteGroup", response.status(), response.body());
    }

    public Map<String, Object> createChildGroup(String realm, String parentGroupId, Map<String, Object> payload) {
        var response = postResponse(paths.group(realm, parentGroupId) + "/children", payload);
        return Map.of("groupId", locationId(response.headers()), "status", response.status(), "parentGroupId", parentGroupId);
    }

    public JsonNode listMembers(String realm, String groupId, Map<String, Object> query) {
        return get(paths.group(realm, groupId) + "/members", query);
    }

    public Map<String, Object> addMember(String realm, String groupId, String userId) {
        var response = putResponse(paths.user(realm, userId) + "/groups/" + groupId, Map.of());
        return actionResult("addGroupMember", response.status(), response.body());
    }

    public Map<String, Object> removeMember(String realm, String groupId, String userId) {
        var response = deleteResponse(paths.user(realm, userId) + "/groups/" + groupId);
        return actionResult("removeGroupMember", response.status(), response.body());
    }

    public JsonNode hierarchy(String realm) {
        return get(paths.groups(realm), Map.of("briefRepresentation", false));
    }

    public Map<String, Object> updateAttributes(String realm, String groupId, Map<String, List<String>> attributes) {
        return updateGroup(realm, groupId, Map.of("attributes", attributes));
    }
}
