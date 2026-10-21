package io.github.sakc.keycloak.mcp.tool.provider;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import io.github.sakc.keycloak.mcp.domain.service.ClientScopeAdminService;
import io.github.sakc.keycloak.mcp.domain.service.ToolExecutionTemplate;
import io.github.sakc.keycloak.mcp.tool.KeycloakToolHandler;
import io.github.sakc.keycloak.mcp.tool.KeycloakToolHandlerProvider;
import io.github.sakc.keycloak.mcp.tool.SimpleKeycloakToolHandler;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import org.springframework.stereotype.Component;

@Component
public class ClientScopeToolProvider implements KeycloakToolHandlerProvider {

    private final List<KeycloakToolHandler<?>> handlers;

    public ClientScopeToolProvider(ClientScopeAdminService service, ToolExecutionTemplate executionTemplate) {
        this.handlers = List.of(
            new SimpleKeycloakToolHandler<>("list_client_scopes", "List client scopes in a realm", RealmRequest.class,
                (request, context) -> executionTemplate.execute("list_client_scopes", request.realm(), false, request,
                    () -> service.listClientScopes(request.realm()))),
            new SimpleKeycloakToolHandler<>("get_client_scope", "Get a client scope by id", ScopeLookupRequest.class,
                (request, context) -> executionTemplate.execute("get_client_scope", request.realm(), false, request,
                    () -> service.getClientScope(request.realm(), request.scopeId()))),
            new SimpleKeycloakToolHandler<>("create_client_scope", "Create a client scope", ScopePayloadRequest.class,
                (request, context) -> executionTemplate.execute("create_client_scope", request.realm(), true, request,
                    () -> service.createClientScope(request.realm(), request.payload()))),
            new SimpleKeycloakToolHandler<>("update_client_scope", "Update a client scope", ScopeUpdateRequest.class,
                (request, context) -> executionTemplate.execute("update_client_scope", request.realm(), true, request,
                    () -> service.updateClientScope(request.realm(), request.scopeId(), request.payload()))),
            new SimpleKeycloakToolHandler<>("delete_client_scope", "Delete a client scope", ScopeLookupRequest.class,
                (request, context) -> executionTemplate.execute("delete_client_scope", request.realm(), true, request,
                    () -> service.deleteClientScope(request.realm(), request.scopeId()))),
            new SimpleKeycloakToolHandler<>("list_client_scope_mappers", "List protocol mappers for a client scope", ScopeLookupRequest.class,
                (request, context) -> executionTemplate.execute("list_client_scope_mappers", request.realm(), false, request,
                    () -> service.listProtocolMappers(request.realm(), request.scopeId()))),
            new SimpleKeycloakToolHandler<>("create_client_scope_mapper", "Create a protocol mapper for a client scope", ScopeMapperCreateRequest.class,
                (request, context) -> executionTemplate.execute("create_client_scope_mapper", request.realm(), true, request,
                    () -> service.createProtocolMapper(request.realm(), request.scopeId(), request.payload()))),
            new SimpleKeycloakToolHandler<>("attach_default_scopes", "Attach default client scopes to a realm", ScopeAttachRequest.class,
                (request, context) -> executionTemplate.execute("attach_default_scopes", request.realm(), true, request,
                    () -> service.attachDefaultScopes(request.realm(), request.scopeIds()))),
            new SimpleKeycloakToolHandler<>("attach_optional_scopes", "Attach optional client scopes to a realm", ScopeAttachRequest.class,
                (request, context) -> executionTemplate.execute("attach_optional_scopes", request.realm(), true, request,
                    () -> service.attachOptionalScopes(request.realm(), request.scopeIds())))
        );
    }

    @Override
    public Collection<? extends KeycloakToolHandler<?>> handlers() {
        return handlers;
    }

    public record RealmRequest(@NotBlank String realm) { }
    public record ScopeLookupRequest(@NotBlank String realm, @NotBlank String scopeId) { }
    public record ScopePayloadRequest(@NotBlank String realm, @NotNull Map<String, Object> payload) { }
    public record ScopeUpdateRequest(@NotBlank String realm, @NotBlank String scopeId, @NotNull Map<String, Object> payload) { }
    public record ScopeMapperCreateRequest(@NotBlank String realm, @NotBlank String scopeId, @NotNull Map<String, Object> payload) { }
    public record ScopeAttachRequest(@NotBlank String realm, @NotEmpty List<String> scopeIds) { }
}
