package io.github.sakc.keycloak.mcp.domain.service;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.sakc.keycloak.mcp.domain.client.KeycloakAdminClient;

import org.springframework.stereotype.Service;

@Service
public class EventAdminService extends AbstractKeycloakDomainService {

    public EventAdminService(KeycloakAdminClient keycloakAdminClient, KeycloakPathBuilder paths) {
        super(keycloakAdminClient, paths);
    }

    public JsonNode queryEvents(String realm, Map<String, Object> query) {
        return get(paths.events(realm), query);
    }

    public JsonNode queryAdminEvents(String realm, Map<String, Object> query) {
        return get(paths.adminEvents(realm), query);
    }

    public Map<String, Object> updateEventConfig(String realm, Map<String, Object> payload) {
        var response = putResponse(paths.realm(realm) + "/events/config", payload);
        return actionResult("updateEventConfig", response.status(), response.body());
    }

    public Map<String, Object> clearEvents(String realm) {
        var response = deleteResponse(paths.events(realm));
        return actionResult("clearEvents", response.status(), response.body());
    }

    public Map<String, Object> clearAdminEvents(String realm) {
        var response = deleteResponse(paths.adminEvents(realm));
        return actionResult("clearAdminEvents", response.status(), response.body());
    }
}
