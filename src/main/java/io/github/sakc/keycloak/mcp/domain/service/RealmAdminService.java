package io.github.sakc.keycloak.mcp.domain.service;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.sakc.keycloak.mcp.domain.client.KeycloakAdminClient;
import io.github.sakc.keycloak.mcp.domain.dto.realm.RealmRepresentation;

import org.springframework.stereotype.Service;

@Service
public class RealmAdminService extends AbstractKeycloakDomainService {

    public RealmAdminService(KeycloakAdminClient keycloakAdminClient, KeycloakPathBuilder paths) {
        super(keycloakAdminClient, paths);
    }

    public JsonNode listRealms() {
        return get("/admin/realms");
    }

    public JsonNode getRealm(String realm) {
        return get(paths.realm(realm));
    }

    public Map<String, Object> createRealm(RealmRepresentation payload) {
        var response = postResponse("/admin/realms", payload);
        return Map.of("realm", payload.realm(), "status", response.status());
    }

    public Map<String, Object> updateRealm(String realm, RealmRepresentation payload) {
        var response = putResponse(paths.realm(realm), payload);
        return actionResult("updateRealm", response.status(), response.body());
    }

    public Map<String, Object> deleteRealm(String realm) {
        var response = deleteResponse(paths.realm(realm));
        return actionResult("deleteRealm", response.status(), response.body());
    }

    public JsonNode authenticationFlows(String realm) {
        return get(paths.realm(realm) + "/authentication/flows");
    }

    public Map<String, Object> updateRealmSecurity(String realm, RealmRepresentation payload) {
        return updateRealm(realm, payload);
    }

    public Map<String, Object> updateLoginSettings(String realm, RealmRepresentation payload) {
        return updateRealm(realm, payload);
    }
}
