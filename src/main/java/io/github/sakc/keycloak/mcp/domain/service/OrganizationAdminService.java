package io.github.sakc.keycloak.mcp.domain.service;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.sakc.keycloak.mcp.domain.client.KeycloakAdminClient;

import org.springframework.stereotype.Service;

@Service
public class OrganizationAdminService extends AbstractKeycloakDomainService {

    public OrganizationAdminService(KeycloakAdminClient keycloakAdminClient, KeycloakPathBuilder paths) {
        super(keycloakAdminClient, paths);
    }

    public JsonNode listOrganizations(String realm, Map<String, Object> query) {
        return get(paths.organizations(realm), query);
    }

    public JsonNode getOrganization(String realm, String organizationId) {
        return get(paths.organization(realm, organizationId));
    }

    public Map<String, Object> createOrganization(String realm, Map<String, Object> payload) {
        var response = postResponse(paths.organizations(realm), payload);
        return Map.of("organizationId", locationId(response.headers()), "status", response.status());
    }

    public Map<String, Object> updateOrganization(String realm, String organizationId, Map<String, Object> payload) {
        var response = putResponse(paths.organization(realm, organizationId), payload);
        return actionResult("updateOrganization", response.status(), response.body());
    }

    public Map<String, Object> deleteOrganization(String realm, String organizationId) {
        var response = deleteResponse(paths.organization(realm, organizationId));
        return actionResult("deleteOrganization", response.status(), response.body());
    }

    public JsonNode listMembers(String realm, String organizationId) {
        return get(paths.organization(realm, organizationId) + "/members");
    }

    public Map<String, Object> addMember(String realm, String organizationId, String userId) {
        var response = postResponse(paths.organization(realm, organizationId) + "/members", Map.of("userId", userId));
        return actionResult("addOrganizationMember", response.status(), response.body());
    }

    public Map<String, Object> removeMember(String realm, String organizationId, String userId) {
        var response = deleteResponse(paths.organization(realm, organizationId) + "/members/" + userId);
        return actionResult("removeOrganizationMember", response.status(), response.body());
    }
}
