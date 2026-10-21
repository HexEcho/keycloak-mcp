package io.github.sakc.keycloak.mcp.tool.provider;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import io.github.sakc.keycloak.mcp.domain.service.GroupAdminService;
import io.github.sakc.keycloak.mcp.domain.service.ToolExecutionTemplate;
import io.github.sakc.keycloak.mcp.tool.KeycloakToolHandler;
import io.github.sakc.keycloak.mcp.tool.KeycloakToolHandlerProvider;
import io.github.sakc.keycloak.mcp.tool.SimpleKeycloakToolHandler;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import org.springframework.stereotype.Component;

@Component
public class GroupToolProvider implements KeycloakToolHandlerProvider {

    private final List<KeycloakToolHandler<?>> handlers;

    public GroupToolProvider(GroupAdminService service, ToolExecutionTemplate executionTemplate) {
        this.handlers = List.of(
            new SimpleKeycloakToolHandler<>("list_groups", "List groups in a Keycloak realm", GroupQueryRequest.class,
                (request, context) -> executionTemplate.execute("list_groups", request.realm(), false, request,
                    () -> service.listGroups(request.realm(), request.toQueryMap()))),
            new SimpleKeycloakToolHandler<>("get_group", "Get a Keycloak group by id", GroupLookupRequest.class,
                (request, context) -> executionTemplate.execute("get_group", request.realm(), false, request,
                    () -> service.getGroup(request.realm(), request.groupId()))),
            new SimpleKeycloakToolHandler<>("create_group", "Create a Keycloak group", GroupPayloadRequest.class,
                (request, context) -> executionTemplate.execute("create_group", request.realm(), true, request,
                    () -> service.createGroup(request.realm(), request.payload()))),
            new SimpleKeycloakToolHandler<>("update_group", "Update a Keycloak group", GroupUpdateRequest.class,
                (request, context) -> executionTemplate.execute("update_group", request.realm(), true, request,
                    () -> service.updateGroup(request.realm(), request.groupId(), request.payload()))),
            new SimpleKeycloakToolHandler<>("delete_group", "Delete a Keycloak group", GroupLookupRequest.class,
                (request, context) -> executionTemplate.execute("delete_group", request.realm(), true, request,
                    () -> service.deleteGroup(request.realm(), request.groupId()))),
            new SimpleKeycloakToolHandler<>("create_child_group", "Create a nested child group", ChildGroupRequest.class,
                (request, context) -> executionTemplate.execute("create_child_group", request.realm(), true, request,
                    () -> service.createChildGroup(request.realm(), request.parentGroupId(), request.payload()))),
            new SimpleKeycloakToolHandler<>("list_group_members", "List members of a group", GroupMembersRequest.class,
                (request, context) -> executionTemplate.execute("list_group_members", request.realm(), false, request,
                    () -> service.listMembers(request.realm(), request.groupId(), request.toQueryMap()))),
            new SimpleKeycloakToolHandler<>("add_group_member", "Add a user to a group", GroupMemberMutationRequest.class,
                (request, context) -> executionTemplate.execute("add_group_member", request.realm(), true, request,
                    () -> service.addMember(request.realm(), request.groupId(), request.userId()))),
            new SimpleKeycloakToolHandler<>("remove_group_member", "Remove a user from a group", GroupMemberMutationRequest.class,
                (request, context) -> executionTemplate.execute("remove_group_member", request.realm(), true, request,
                    () -> service.removeMember(request.realm(), request.groupId(), request.userId()))),
            new SimpleKeycloakToolHandler<>("group_hierarchy", "Get the full group hierarchy for a realm", RealmRequest.class,
                (request, context) -> executionTemplate.execute("group_hierarchy", request.realm(), false, request,
                    () -> service.hierarchy(request.realm()))),
            new SimpleKeycloakToolHandler<>("update_group_attributes", "Update group attributes", GroupAttributesRequest.class,
                (request, context) -> executionTemplate.execute("update_group_attributes", request.realm(), true, request,
                    () -> service.updateAttributes(request.realm(), request.groupId(), request.attributes())))
        );
    }

    @Override
    public Collection<? extends KeycloakToolHandler<?>> handlers() {
        return handlers;
    }

    public record RealmRequest(@NotBlank String realm) { }
    public record GroupLookupRequest(@NotBlank String realm, @NotBlank String groupId) { }
    public record GroupPayloadRequest(@NotBlank String realm, @NotNull Map<String, Object> payload) { }
    public record GroupUpdateRequest(@NotBlank String realm, @NotBlank String groupId, @NotNull Map<String, Object> payload) { }
    public record ChildGroupRequest(@NotBlank String realm, @NotBlank String parentGroupId, @NotNull Map<String, Object> payload) { }
    public record GroupMemberMutationRequest(@NotBlank String realm, @NotBlank String groupId, @NotBlank String userId) { }
    public record GroupAttributesRequest(@NotBlank String realm, @NotBlank String groupId, @NotNull Map<String, List<String>> attributes) { }
    public record GroupMembersRequest(@NotBlank String realm, @NotBlank String groupId, Integer first, Integer max) {
        public Map<String, Object> toQueryMap() {
            return Map.of("first", first == null ? 0 : first, "max", max == null ? 50 : max);
        }
    }
    public record GroupQueryRequest(@NotBlank String realm, String search, Integer first, Integer max, Boolean briefRepresentation) {
        public Map<String, Object> toQueryMap() {
            return Map.of(
                "search", search == null ? "" : search,
                "first", first == null ? 0 : first,
                "max", max == null ? 50 : max,
                "briefRepresentation", briefRepresentation == null ? true : briefRepresentation
            );
        }
    }
}
