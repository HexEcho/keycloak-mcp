package io.github.sakc.keycloak.mcp.domain.service;

import org.springframework.stereotype.Component;

@Component
public class KeycloakPathBuilder {

    public String realm(String realm) {
        return "/admin/realms/" + realm;
    }

    public String users(String realm) {
        return realm(realm) + "/users";
    }

    public String user(String realm, String userId) {
        return users(realm) + "/" + userId;
    }

    public String clients(String realm) {
        return realm(realm) + "/clients";
    }

    public String client(String realm, String clientUuid) {
        return clients(realm) + "/" + clientUuid;
    }

    public String groups(String realm) {
        return realm(realm) + "/groups";
    }

    public String group(String realm, String groupId) {
        return groups(realm) + "/" + groupId;
    }

    public String roles(String realm) {
        return realm(realm) + "/roles";
    }

    public String role(String realm, String roleName) {
        return roles(realm) + "/" + roleName;
    }

    public String clientRoles(String realm, String clientUuid) {
        return client(realm, clientUuid) + "/roles";
    }

    public String clientRole(String realm, String clientUuid, String roleName) {
        return clientRoles(realm, clientUuid) + "/" + roleName;
    }

    public String organizations(String realm) {
        return realm(realm) + "/organizations";
    }

    public String organization(String realm, String organizationId) {
        return organizations(realm) + "/" + organizationId;
    }

    public String identityProviders(String realm) {
        return realm(realm) + "/identity-provider/instances";
    }

    public String identityProvider(String realm, String alias) {
        return identityProviders(realm) + "/" + alias;
    }

    public String clientScopes(String realm) {
        return realm(realm) + "/client-scopes";
    }

    public String clientScope(String realm, String scopeId) {
        return clientScopes(realm) + "/" + scopeId;
    }

    public String sessions(String realm) {
        return realm(realm) + "/sessions";
    }

    public String events(String realm) {
        return realm(realm) + "/events";
    }

    public String adminEvents(String realm) {
        return realm(realm) + "/admin-events";
    }
}
