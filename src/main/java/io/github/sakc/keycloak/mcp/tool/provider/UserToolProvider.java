package io.github.sakc.keycloak.mcp.tool.provider;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import io.github.sakc.keycloak.mcp.domain.dto.user.CredentialRepresentation;
import io.github.sakc.keycloak.mcp.domain.dto.user.RoleMappingRepresentation;
import io.github.sakc.keycloak.mcp.domain.dto.user.UserRepresentation;
import io.github.sakc.keycloak.mcp.domain.service.ToolExecutionTemplate;
import io.github.sakc.keycloak.mcp.domain.service.UserAdminService;
import io.github.sakc.keycloak.mcp.tool.KeycloakToolHandler;
import io.github.sakc.keycloak.mcp.tool.KeycloakToolHandlerProvider;
import io.github.sakc.keycloak.mcp.tool.SimpleKeycloakToolHandler;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;

import org.springframework.stereotype.Component;

@Component
public class UserToolProvider implements KeycloakToolHandlerProvider {

    private final List<KeycloakToolHandler<?>> handlers;

    public UserToolProvider(UserAdminService service, ToolExecutionTemplate executionTemplate) {
        this.handlers = List.of(
            new SimpleKeycloakToolHandler<>("create_user", "Create a user in a Keycloak realm", CreateUserRequest.class,
                (request, context) -> executionTemplate.execute("create_user", request.realm(), true, request,
                    () -> service.createUser(request.realm(), request.payload().toRepresentation()))),
            new SimpleKeycloakToolHandler<>("update_user", "Update an existing Keycloak user", UpdateUserRequest.class,
                (request, context) -> executionTemplate.execute("update_user", request.realm(), true, request,
                    () -> service.updateUser(request.realm(), request.userId(), request.payload().toRepresentation()))),
            new SimpleKeycloakToolHandler<>("delete_user", "Delete a Keycloak user", UserLookupRequest.class,
                (request, context) -> executionTemplate.execute("delete_user", request.realm(), true, request,
                    () -> service.deleteUser(request.realm(), request.userId()))),
            new SimpleKeycloakToolHandler<>("search_users", "Search Keycloak users by query parameters", SearchUsersRequest.class,
                (request, context) -> executionTemplate.execute("search_users", request.realm(), false, request,
                    () -> service.searchUsers(request.realm(), request.toQueryMap()))),
            new SimpleKeycloakToolHandler<>("list_users", "List Keycloak users in a realm", ListUsersRequest.class,
                (request, context) -> executionTemplate.execute("list_users", request.realm(), false, request,
                    () -> service.searchUsers(request.realm(), request.toQueryMap()))),
            new SimpleKeycloakToolHandler<>("get_user", "Get a Keycloak user by id", UserLookupRequest.class,
                (request, context) -> executionTemplate.execute("get_user", request.realm(), false, request,
                    () -> service.getUser(request.realm(), request.userId()))),
            new SimpleKeycloakToolHandler<>("reset_password", "Reset a Keycloak user password", ResetPasswordRequest.class,
                (request, context) -> executionTemplate.execute("reset_password", request.realm(), true, request,
                    () -> service.resetPassword(request.realm(), request.userId(), request.passwordCredential()))),
            new SimpleKeycloakToolHandler<>("enable_user", "Enable a Keycloak user", UserLookupRequest.class,
                (request, context) -> executionTemplate.execute("enable_user", request.realm(), true, request,
                    () -> service.setEnabled(request.realm(), request.userId(), true))),
            new SimpleKeycloakToolHandler<>("disable_user", "Disable a Keycloak user", UserLookupRequest.class,
                (request, context) -> executionTemplate.execute("disable_user", request.realm(), true, request,
                    () -> service.setEnabled(request.realm(), request.userId(), false))),
            new SimpleKeycloakToolHandler<>("lock_user", "Lock a Keycloak user by disabling the account", UserLookupRequest.class,
                (request, context) -> executionTemplate.execute("lock_user", request.realm(), true, request,
                    () -> service.setEnabled(request.realm(), request.userId(), false))),
            new SimpleKeycloakToolHandler<>("unlock_user", "Unlock a Keycloak user from brute force protection", UserLookupRequest.class,
                (request, context) -> executionTemplate.execute("unlock_user", request.realm(), true, request,
                    () -> service.unlockUser(request.realm(), request.userId()))),
            new SimpleKeycloakToolHandler<>("logout_user_sessions", "Logout all active sessions for a user", UserLookupRequest.class,
                (request, context) -> executionTemplate.execute("logout_user_sessions", request.realm(), true, request,
                    () -> service.logoutSessions(request.realm(), request.userId()))),
            new SimpleKeycloakToolHandler<>("send_verification_email", "Send verification email or required actions to a user", ExecuteActionsRequest.class,
                (request, context) -> executionTemplate.execute("send_verification_email", request.realm(), true, request,
                    () -> service.executeActions(request.realm(), request.userId(), request.actions(), request.lifespan(), request.redirectUri(), request.clientId()))),
            new SimpleKeycloakToolHandler<>("execute_required_actions", "Execute required actions for a Keycloak user", ExecuteActionsRequest.class,
                (request, context) -> executionTemplate.execute("execute_required_actions", request.realm(), true, request,
                    () -> service.executeActions(request.realm(), request.userId(), request.actions(), request.lifespan(), request.redirectUri(), request.clientId()))),
            new SimpleKeycloakToolHandler<>("assign_realm_roles", "Assign realm roles to a Keycloak user", AssignRolesRequest.class,
                (request, context) -> executionTemplate.execute("assign_realm_roles", request.realm(), true, request,
                    () -> service.assignRealmRoles(request.realm(), request.userId(), request.roles()))),
            new SimpleKeycloakToolHandler<>("remove_realm_roles", "Remove realm roles from a Keycloak user", AssignRolesRequest.class,
                (request, context) -> executionTemplate.execute("remove_realm_roles", request.realm(), true, request,
                    () -> service.removeRealmRoles(request.realm(), request.userId(), request.roles()))),
            new SimpleKeycloakToolHandler<>("assign_client_roles", "Assign client roles to a Keycloak user", AssignClientRolesRequest.class,
                (request, context) -> executionTemplate.execute("assign_client_roles", request.realm(), true, request,
                    () -> service.assignClientRoles(request.realm(), request.userId(), request.clientUuid(), request.roles()))),
            new SimpleKeycloakToolHandler<>("remove_client_roles", "Remove client roles from a Keycloak user", AssignClientRolesRequest.class,
                (request, context) -> executionTemplate.execute("remove_client_roles", request.realm(), true, request,
                    () -> service.removeClientRoles(request.realm(), request.userId(), request.clientUuid(), request.roles()))),
            new SimpleKeycloakToolHandler<>("join_groups", "Add a Keycloak user to one or more groups", GroupMembershipRequest.class,
                (request, context) -> executionTemplate.execute("join_groups", request.realm(), true, request,
                    () -> service.joinGroups(request.realm(), request.userId(), request.groupIds()))),
            new SimpleKeycloakToolHandler<>("remove_from_groups", "Remove a Keycloak user from one or more groups", GroupMembershipRequest.class,
                (request, context) -> executionTemplate.execute("remove_from_groups", request.realm(), true, request,
                    () -> service.leaveGroups(request.realm(), request.userId(), request.groupIds()))),
            new SimpleKeycloakToolHandler<>("update_user_attributes", "Update Keycloak user attributes", UpdateUserAttributesRequest.class,
                (request, context) -> executionTemplate.execute("update_user_attributes", request.realm(), true, request,
                    () -> service.updateAttributes(request.realm(), request.userId(), request.attributes()))),
            new SimpleKeycloakToolHandler<>("list_user_credentials", "List credentials configured for a Keycloak user", UserLookupRequest.class,
                (request, context) -> executionTemplate.execute("list_user_credentials", request.realm(), false, request,
                    () -> service.listCredentials(request.realm(), request.userId()))),
            new SimpleKeycloakToolHandler<>("list_federated_identities", "List federated identities for a Keycloak user", UserLookupRequest.class,
                (request, context) -> executionTemplate.execute("list_federated_identities", request.realm(), false, request,
                    () -> service.listFederatedIdentities(request.realm(), request.userId())))
        );
    }

    @Override
    public Collection<? extends KeycloakToolHandler<?>> handlers() {
        return handlers;
    }

    public record CreateUserRequest(@NotBlank String realm, @NotNull @Valid CreateUserPayload payload) { }

    public record CreateUserPayload(
        @NotBlank String username,
        String firstName,
        String lastName,
        String email,
        Boolean enabled,
        Boolean emailVerified,
        Map<String, List<String>> attributes,
        List<CredentialRepresentation> credentials,
        List<String> requiredActions,
        Long createdTimestamp
    ) {
        public UserRepresentation toRepresentation() {
            return new UserRepresentation(null, username, firstName, lastName, email, enabled, emailVerified, attributes, credentials, requiredActions, createdTimestamp);
        }
    }

    public record UpdateUserRequest(@NotBlank String realm, @NotBlank String userId, @NotNull @Valid UpdateUserPayload payload) { }

    public record UpdateUserPayload(
        String username,
        String firstName,
        String lastName,
        String email,
        Boolean enabled,
        Boolean emailVerified,
        Map<String, List<String>> attributes,
        List<CredentialRepresentation> credentials,
        List<String> requiredActions,
        Long createdTimestamp
    ) {
        public UserRepresentation toRepresentation() {
            return new UserRepresentation(null, username, firstName, lastName, email, enabled, emailVerified, attributes, credentials, requiredActions, createdTimestamp);
        }
    }

    public record UserLookupRequest(@NotBlank String realm, @NotBlank String userId) { }

    public record ResetPasswordRequest(@NotBlank String realm, @NotBlank String userId, @NotNull @Valid PasswordCredential password) {
        public CredentialRepresentation passwordCredential() {
            return new CredentialRepresentation(null, password.type(), password.value(), password.temporary(), null, null, null);
        }
    }

    public record PasswordCredential(@NotBlank String type, @NotBlank String value, boolean temporary) { }

    public record ExecuteActionsRequest(@NotBlank String realm, @NotBlank String userId, @NotEmpty List<String> actions, Integer lifespan, String redirectUri, String clientId) { }

    public record AssignRolesRequest(@NotBlank String realm, @NotBlank String userId, @NotEmpty @Valid List<RoleMappingRepresentation> roles) { }

    public record AssignClientRolesRequest(@NotBlank String realm, @NotBlank String userId, @NotBlank String clientUuid, @NotEmpty @Valid List<RoleMappingRepresentation> roles) { }

    public record GroupMembershipRequest(@NotBlank String realm, @NotBlank String userId, @NotEmpty List<String> groupIds) { }

    public record UpdateUserAttributesRequest(@NotBlank String realm, @NotBlank String userId, @NotNull Map<String, List<String>> attributes) { }

    public record SearchUsersRequest(@NotBlank String realm, String search, String email, String username, String firstName, String lastName, Integer first, Integer max, Boolean enabled, Boolean exact) {
        public Map<String, Object> toQueryMap() {
            return Map.ofEntries(
                Map.entry("search", search == null ? "" : search),
                Map.entry("email", email == null ? "" : email),
                Map.entry("username", username == null ? "" : username),
                Map.entry("firstName", firstName == null ? "" : firstName),
                Map.entry("lastName", lastName == null ? "" : lastName),
                Map.entry("first", first == null ? 0 : first),
                Map.entry("max", max == null ? 20 : max),
                Map.entry("enabled", enabled == null ? "" : enabled),
                Map.entry("exact", exact == null ? "" : exact)
            );
        }
    }

    public record ListUsersRequest(@NotBlank String realm, Integer first, Integer max) {
        public Map<String, Object> toQueryMap() {
            return Map.of("first", first == null ? 0 : first, "max", max == null ? 50 : max);
        }
    }
}
