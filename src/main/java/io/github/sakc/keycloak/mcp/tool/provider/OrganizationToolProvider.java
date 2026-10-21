package io.github.sakc.keycloak.mcp.tool.provider;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import io.github.sakc.keycloak.mcp.domain.service.OrganizationAdminService;
import io.github.sakc.keycloak.mcp.domain.service.ToolExecutionTemplate;
import io.github.sakc.keycloak.mcp.tool.KeycloakToolHandler;
import io.github.sakc.keycloak.mcp.tool.KeycloakToolHandlerProvider;
import io.github.sakc.keycloak.mcp.tool.SimpleKeycloakToolHandler;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.stereotype.Component;

@Component
public class OrganizationToolProvider implements KeycloakToolHandlerProvider {

    private final List<KeycloakToolHandler<?>> handlers;

    public OrganizationToolProvider(OrganizationAdminService service, ToolExecutionTemplate executionTemplate) {
        this.handlers = List.of(
            new SimpleKeycloakToolHandler<>("list_organizations", "List organizations in a realm", OrganizationQueryRequest.class,
                (request, context) -> executionTemplate.execute("list_organizations", request.realm(), false, request,
                    () -> service.listOrganizations(request.realm(), request.toQueryMap()))),
            new SimpleKeycloakToolHandler<>("get_organization", "Get a Keycloak organization by id", OrganizationLookupRequest.class,
                (request, context) -> executionTemplate.execute("get_organization", request.realm(), false, request,
                    () -> service.getOrganization(request.realm(), request.organizationId()))),
            new SimpleKeycloakToolHandler<>("create_organization", "Create a Keycloak organization", OrganizationPayloadRequest.class,
                (request, context) -> executionTemplate.execute("create_organization", request.realm(), true, request,
                    () -> service.createOrganization(request.realm(), request.payload()))),
            new SimpleKeycloakToolHandler<>("update_organization", "Update a Keycloak organization", OrganizationUpdateRequest.class,
                (request, context) -> executionTemplate.execute("update_organization", request.realm(), true, request,
                    () -> service.updateOrganization(request.realm(), request.organizationId(), request.payload()))),
            new SimpleKeycloakToolHandler<>("delete_organization", "Delete a Keycloak organization", OrganizationLookupRequest.class,
                (request, context) -> executionTemplate.execute("delete_organization", request.realm(), true, request,
                    () -> service.deleteOrganization(request.realm(), request.organizationId()))),
            new SimpleKeycloakToolHandler<>("list_organization_members", "List members of a Keycloak organization", OrganizationLookupRequest.class,
                (request, context) -> executionTemplate.execute("list_organization_members", request.realm(), false, request,
                    () -> service.listMembers(request.realm(), request.organizationId()))),
            new SimpleKeycloakToolHandler<>("add_organization_member", "Add a member to an organization", OrganizationMemberRequest.class,
                (request, context) -> executionTemplate.execute("add_organization_member", request.realm(), true, request,
                    () -> service.addMember(request.realm(), request.organizationId(), request.userId()))),
            new SimpleKeycloakToolHandler<>("remove_organization_member", "Remove a member from an organization", OrganizationMemberRequest.class,
                (request, context) -> executionTemplate.execute("remove_organization_member", request.realm(), true, request,
                    () -> service.removeMember(request.realm(), request.organizationId(), request.userId())))
        );
    }

    @Override
    public Collection<? extends KeycloakToolHandler<?>> handlers() {
        return handlers;
    }

    public record OrganizationQueryRequest(@NotBlank String realm, String search) {
        public Map<String, Object> toQueryMap() {
            return Map.of("search", search == null ? "" : search);
        }
    }
    public record OrganizationLookupRequest(@NotBlank String realm, @NotBlank String organizationId) { }
    public record OrganizationPayloadRequest(@NotBlank String realm, @NotNull Map<String, Object> payload) { }
    public record OrganizationUpdateRequest(@NotBlank String realm, @NotBlank String organizationId, @NotNull Map<String, Object> payload) { }
    public record OrganizationMemberRequest(@NotBlank String realm, @NotBlank String organizationId, @NotBlank String userId) { }
}
