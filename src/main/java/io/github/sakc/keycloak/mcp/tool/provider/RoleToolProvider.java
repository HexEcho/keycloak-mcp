package io.github.sakc.keycloak.mcp.tool.provider;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import io.github.sakc.keycloak.mcp.domain.service.RoleAdminService;
import io.github.sakc.keycloak.mcp.domain.service.ToolExecutionTemplate;
import io.github.sakc.keycloak.mcp.tool.KeycloakToolHandler;
import io.github.sakc.keycloak.mcp.tool.KeycloakToolHandlerProvider;
import io.github.sakc.keycloak.mcp.tool.SimpleKeycloakToolHandler;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import org.springframework.stereotype.Component;

@Component
public class RoleToolProvider implements KeycloakToolHandlerProvider {

    private final List<KeycloakToolHandler<?>> handlers;

    public RoleToolProvider(RoleAdminService service, ToolExecutionTemplate executionTemplate) {
        this.handlers = List.of(
            new SimpleKeycloakToolHandler<>("list_realm_roles", "List realm roles", RealmRequest.class,
                (request, context) -> executionTemplate.execute("list_realm_roles", request.realm(), false, request,
                    () -> service.listRealmRoles(request.realm()))),
            new SimpleKeycloakToolHandler<>("create_realm_role", "Create a realm role", RoleCreateRequest.class,
                (request, context) -> executionTemplate.execute("create_realm_role", request.realm(), true, request,
                    () -> service.createRealmRole(request.realm(), request.payload()))),
            new SimpleKeycloakToolHandler<>("get_realm_role", "Get a realm role", RoleLookupRequest.class,
                (request, context) -> executionTemplate.execute("get_realm_role", request.realm(), false, request,
                    () -> service.getRealmRole(request.realm(), request.roleName()))),
            new SimpleKeycloakToolHandler<>("update_realm_role", "Update a realm role", RoleUpdateRequest.class,
                (request, context) -> executionTemplate.execute("update_realm_role", request.realm(), true, request,
                    () -> service.updateRealmRole(request.realm(), request.roleName(), request.payload()))),
            new SimpleKeycloakToolHandler<>("delete_realm_role", "Delete a realm role", RoleLookupRequest.class,
                (request, context) -> executionTemplate.execute("delete_realm_role", request.realm(), true, request,
                    () -> service.deleteRealmRole(request.realm(), request.roleName()))),
            new SimpleKeycloakToolHandler<>("list_client_roles", "List client roles", ClientRoleRealmRequest.class,
                (request, context) -> executionTemplate.execute("list_client_roles", request.realm(), false, request,
                    () -> service.listClientRoles(request.realm(), request.clientUuid()))),
            new SimpleKeycloakToolHandler<>("create_client_role", "Create a client role", ClientRoleCreateRequest.class,
                (request, context) -> executionTemplate.execute("create_client_role", request.realm(), true, request,
                    () -> service.createClientRole(request.realm(), request.clientUuid(), request.payload()))),
            new SimpleKeycloakToolHandler<>("assign_composite_roles", "Assign composite roles to a realm role", CompositeRolesRequest.class,
                (request, context) -> executionTemplate.execute("assign_composite_roles", request.realm(), true, request,
                    () -> service.assignCompositeRoles(request.realm(), request.roleName(), request.roles()))),
            new SimpleKeycloakToolHandler<>("remove_composite_roles", "Remove composite roles from a realm role", CompositeRolesRequest.class,
                (request, context) -> executionTemplate.execute("remove_composite_roles", request.realm(), true, request,
                    () -> service.removeCompositeRoles(request.realm(), request.roleName(), request.roles()))),
            new SimpleKeycloakToolHandler<>("users_with_role", "List users assigned to a realm role", RoleLookupRequest.class,
                (request, context) -> executionTemplate.execute("users_with_role", request.realm(), false, request,
                    () -> service.usersWithRole(request.realm(), request.roleName()))),
            new SimpleKeycloakToolHandler<>("groups_with_role", "List groups assigned to a realm role", RoleLookupRequest.class,
                (request, context) -> executionTemplate.execute("groups_with_role", request.realm(), false, request,
                    () -> service.groupsWithRole(request.realm(), request.roleName())))
        );
    }

    @Override
    public Collection<? extends KeycloakToolHandler<?>> handlers() {
        return handlers;
    }

    public record RealmRequest(@NotBlank String realm) { }
    public record RoleLookupRequest(@NotBlank String realm, @NotBlank String roleName) { }
    public record RoleCreateRequest(@NotBlank String realm, @NotNull Map<String, Object> payload) { }
    public record RoleUpdateRequest(@NotBlank String realm, @NotBlank String roleName, @NotNull Map<String, Object> payload) { }
    public record ClientRoleRealmRequest(@NotBlank String realm, @NotBlank String clientUuid) { }
    public record ClientRoleCreateRequest(@NotBlank String realm, @NotBlank String clientUuid, @NotNull Map<String, Object> payload) { }
    public record CompositeRolesRequest(@NotBlank String realm, @NotBlank String roleName, @NotEmpty List<Map<String, Object>> roles) { }
}
