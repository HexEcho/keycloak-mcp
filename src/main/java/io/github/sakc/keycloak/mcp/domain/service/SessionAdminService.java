package io.github.sakc.keycloak.mcp.domain.service;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.sakc.keycloak.mcp.domain.client.KeycloakAdminClient;

import org.springframework.stereotype.Service;

@Service
public class SessionAdminService extends AbstractKeycloakDomainService {

    public SessionAdminService(KeycloakAdminClient keycloakAdminClient, KeycloakPathBuilder paths) {
        super(keycloakAdminClient, paths);
    }

    public JsonNode listActiveSessions(String realm, String clientUuid) {
        return get(paths.client(realm, clientUuid) + "/user-sessions");
    }

    public JsonNode listOfflineSessions(String realm, String clientUuid) {
        return get(paths.client(realm, clientUuid) + "/offline-sessions");
    }

    public JsonNode sessionStatistics(String realm) {
        return get(paths.realm(realm) + "/client-session-stats");
    }

    public Map<String, Object> terminateSession(String realm, String sessionId) {
        var response = deleteResponse(paths.realm(realm) + "/sessions/" + sessionId);
        return actionResult("terminateSession", response.status(), response.body());
    }

    public Map<String, Object> logoutAll(String realm) {
        var response = postResponse(paths.realm(realm) + "/logout-all", Map.of());
        return actionResult("logoutAllSessions", response.status(), response.body());
    }
}
