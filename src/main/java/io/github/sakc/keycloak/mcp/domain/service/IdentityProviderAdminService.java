package io.github.sakc.keycloak.mcp.domain.service;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.sakc.keycloak.mcp.domain.client.KeycloakAdminClient;

import org.springframework.stereotype.Service;

@Service
public class IdentityProviderAdminService extends AbstractKeycloakDomainService {

    public IdentityProviderAdminService(KeycloakAdminClient keycloakAdminClient, KeycloakPathBuilder paths) {
        super(keycloakAdminClient, paths);
    }

    public JsonNode listIdentityProviders(String realm) {
        return get(paths.identityProviders(realm));
    }

    public JsonNode getIdentityProvider(String realm, String alias) {
        return get(paths.identityProvider(realm, alias));
    }

    public Map<String, Object> createIdentityProvider(String realm, Map<String, Object> payload) {
        var response = postResponse(paths.identityProviders(realm), payload);
        return actionResult("createIdentityProvider", response.status(), response.body());
    }

    public Map<String, Object> updateIdentityProvider(String realm, String alias, Map<String, Object> payload) {
        var response = putResponse(paths.identityProvider(realm, alias), payload);
        return actionResult("updateIdentityProvider", response.status(), response.body());
    }

    public Map<String, Object> deleteIdentityProvider(String realm, String alias) {
        var response = deleteResponse(paths.identityProvider(realm, alias));
        return actionResult("deleteIdentityProvider", response.status(), response.body());
    }

    public JsonNode listMappers(String realm, String alias) {
        return get(paths.identityProvider(realm, alias) + "/mappers");
    }

    public Map<String, Object> createMapper(String realm, String alias, Map<String, Object> payload) {
        var response = postResponse(paths.identityProvider(realm, alias) + "/mappers", payload);
        return actionResult("createIdentityProviderMapper", response.status(), response.body());
    }

    public Map<String, Object> updateMapper(String realm, String alias, String mapperId, Map<String, Object> payload) {
        var response = putResponse(paths.identityProvider(realm, alias) + "/mappers/" + mapperId, payload);
        return actionResult("updateIdentityProviderMapper", response.status(), response.body());
    }

    public Map<String, Object> deleteMapper(String realm, String alias, String mapperId) {
        var response = deleteResponse(paths.identityProvider(realm, alias) + "/mappers/" + mapperId);
        return actionResult("deleteIdentityProviderMapper", response.status(), response.body());
    }
}
