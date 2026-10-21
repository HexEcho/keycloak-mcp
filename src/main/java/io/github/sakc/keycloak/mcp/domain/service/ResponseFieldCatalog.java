package io.github.sakc.keycloak.mcp.domain.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

/** Single source of truth for the default and selectable response fields of every read tool. */
@Component
public class ResponseFieldCatalog {

    public record Resource(String resource, String description, List<String> defaultFields, List<String> availableFields) { }

    private static final Map<String, Resource> RESOURCES = new LinkedHashMap<>();
    private static final Map<String, String> TOOL_TO_RESOURCE = new LinkedHashMap<>();

    static {
        resource("client", "Keycloak client (OIDC/SAML application)",
            List.of("id", "clientId", "name", "description"),
            List.of("id", "clientId", "name", "description", "protocol", "enabled", "publicClient", "bearerOnly", "serviceAccountsEnabled",
                "standardFlowEnabled", "implicitFlowEnabled", "directAccessGrantsEnabled", "consentRequired", "fullScopeAllowed",
                "clientAuthenticatorType", "rootUrl", "baseUrl", "adminUrl", "redirectUris", "webOrigins", "defaultClientScopes",
                "optionalClientScopes", "protocolMappers", "attributes", "authenticationFlowBindingOverrides"),
            "list_clients", "search_clients", "get_client");
        resource("realm", "Keycloak realm",
            List.of("id", "realm", "displayName", "enabled"),
            List.of("id", "realm", "displayName", "enabled", "registrationAllowed", "resetPasswordAllowed", "rememberMe", "verifyEmail",
                "bruteForceProtected", "sslRequired", "passwordPolicy", "loginTheme", "accountTheme", "adminTheme", "emailTheme",
                "accessTokenLifespan", "ssoSessionIdleTimeout", "ssoSessionMaxLifespan", "defaultLocale", "smtpServer", "attributes"),
            "list_realms", "get_realm");
        resource("user", "Keycloak user",
            List.of("id", "username", "email", "enabled"),
            List.of("id", "username", "email", "emailVerified", "firstName", "lastName", "enabled", "createdTimestamp", "attributes",
                "requiredActions", "federationLink", "totp", "disableableCredentialTypes", "access"),
            "search_users", "list_users", "get_user", "users_with_role", "list_group_members", "list_organization_members");
        resource("group", "Keycloak group",
            List.of("id", "name", "path"),
            List.of("id", "name", "path", "attributes", "realmRoles", "clientRoles", "subGroupCount", "subGroups"),
            "list_groups", "get_group", "groups_with_role");
        resource("role", "Realm or client role",
            List.of("id", "name", "description"),
            List.of("id", "name", "description", "composite", "clientRole", "containerId", "attributes"),
            "list_realm_roles", "get_realm_role", "list_client_roles");
        resource("client_scope", "Client scope",
            List.of("id", "name", "description", "protocol"),
            List.of("id", "name", "description", "protocol", "attributes", "protocolMappers"),
            "list_client_scopes", "get_client_scope");
        resource("protocol_mapper", "Protocol mapper",
            List.of("id", "name", "protocol", "protocolMapper"),
            List.of("id", "name", "protocol", "protocolMapper", "consentRequired", "config"),
            "list_protocol_mappers", "list_client_scope_mappers");
        resource("identity_provider", "Identity provider",
            List.of("alias", "displayName", "providerId", "enabled"),
            List.of("alias", "internalId", "displayName", "providerId", "enabled", "trustEmail", "storeToken", "linkOnly",
                "firstBrokerLoginFlowAlias", "postBrokerLoginFlowAlias", "config"),
            "list_identity_providers", "get_identity_provider");
        resource("identity_provider_mapper", "Identity provider mapper",
            List.of("id", "name", "identityProviderMapper"),
            List.of("id", "name", "identityProviderAlias", "identityProviderMapper", "config"),
            "list_identity_provider_mappers");
        resource("organization", "Organization",
            List.of("id", "name", "alias", "enabled"),
            List.of("id", "name", "alias", "enabled", "description", "domains", "attributes"),
            "list_organizations", "get_organization");
        resource("session", "User session",
            List.of("id", "username", "start", "lastAccess"),
            List.of("id", "username", "userId", "ipAddress", "start", "lastAccess", "rememberMe", "clients", "transientUser"),
            "list_active_sessions", "list_offline_sessions");
        resource("credential", "User credential metadata (never includes secrets)",
            List.of("id", "type", "userLabel"),
            List.of("id", "type", "userLabel", "createdDate", "priority"),
            "list_user_credentials");
        resource("federated_identity", "User federated identity link",
            List.of("identityProvider", "userName"),
            List.of("identityProvider", "userId", "userName"),
            "list_federated_identities");
        resource("event", "User event",
            List.of("time", "type", "userId", "clientId", "error"),
            List.of("time", "type", "realmId", "clientId", "userId", "sessionId", "ipAddress", "error", "details"),
            "query_events");
        resource("admin_event", "Admin event",
            List.of("time", "operationType", "resourceType", "resourcePath"),
            List.of("time", "realmId", "operationType", "resourceType", "resourcePath", "authDetails", "error", "representation"),
            "query_admin_events");
    }

    private static void resource(String name, String description, List<String> defaults, List<String> available, String... tools) {
        RESOURCES.put(name, new Resource(name, description, defaults, available));
        for (String tool : tools) {
            TOOL_TO_RESOURCE.put(tool, name);
        }
    }

    public boolean supports(String tool) {
        return TOOL_TO_RESOURCE.containsKey(tool);
    }

    public List<String> defaultFields(String tool) {
        return RESOURCES.get(TOOL_TO_RESOURCE.get(tool)).defaultFields();
    }

    /** Describes one tool, or every read tool when {@code tool} is blank. */
    public List<Map<String, Object>> describe(String tool) {
        List<Map<String, Object>> result = new ArrayList<>();
        TOOL_TO_RESOURCE.forEach((toolName, resourceName) -> {
            if (tool == null || tool.isBlank() || tool.equals(toolName)) {
                Resource resource = RESOURCES.get(resourceName);
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("tool", toolName);
                entry.put("resource", resource.resource());
                entry.put("description", resource.description());
                entry.put("defaultFields", resource.defaultFields());
                entry.put("availableFields", resource.availableFields());
                result.add(entry);
            }
        });
        return result;
    }
}
