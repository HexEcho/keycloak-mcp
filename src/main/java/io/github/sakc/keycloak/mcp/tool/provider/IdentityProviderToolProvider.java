package io.github.sakc.keycloak.mcp.tool.provider;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import io.github.sakc.keycloak.mcp.domain.service.IdentityProviderAdminService;
import io.github.sakc.keycloak.mcp.domain.service.ToolExecutionTemplate;
import io.github.sakc.keycloak.mcp.tool.KeycloakToolHandler;
import io.github.sakc.keycloak.mcp.tool.KeycloakToolHandlerProvider;
import io.github.sakc.keycloak.mcp.tool.SimpleKeycloakToolHandler;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.stereotype.Component;

@Component
public class IdentityProviderToolProvider implements KeycloakToolHandlerProvider {

    private final List<KeycloakToolHandler<?>> handlers;

    public IdentityProviderToolProvider(IdentityProviderAdminService service, ToolExecutionTemplate executionTemplate) {
        this.handlers = List.of(
            new SimpleKeycloakToolHandler<>("list_identity_providers", "List identity providers in a realm", RealmRequest.class,
                (request, context) -> executionTemplate.execute("list_identity_providers", request.realm(), false, request,
                    () -> service.listIdentityProviders(request.realm()))),
            new SimpleKeycloakToolHandler<>("get_identity_provider", "Get an identity provider by alias", IdentityProviderLookupRequest.class,
                (request, context) -> executionTemplate.execute("get_identity_provider", request.realm(), false, request,
                    () -> service.getIdentityProvider(request.realm(), request.alias()))),
            new SimpleKeycloakToolHandler<>("create_identity_provider", "Create an identity provider", IdentityProviderPayloadRequest.class,
                (request, context) -> executionTemplate.execute("create_identity_provider", request.realm(), true, request,
                    () -> service.createIdentityProvider(request.realm(), request.payload()))),
            new SimpleKeycloakToolHandler<>("update_identity_provider", "Update an identity provider", IdentityProviderUpdateRequest.class,
                (request, context) -> executionTemplate.execute("update_identity_provider", request.realm(), true, request,
                    () -> service.updateIdentityProvider(request.realm(), request.alias(), request.payload()))),
            new SimpleKeycloakToolHandler<>("delete_identity_provider", "Delete an identity provider", IdentityProviderLookupRequest.class,
                (request, context) -> executionTemplate.execute("delete_identity_provider", request.realm(), true, request,
                    () -> service.deleteIdentityProvider(request.realm(), request.alias()))),
            new SimpleKeycloakToolHandler<>("list_identity_provider_mappers", "List mappers for an identity provider", IdentityProviderLookupRequest.class,
                (request, context) -> executionTemplate.execute("list_identity_provider_mappers", request.realm(), false, request,
                    () -> service.listMappers(request.realm(), request.alias()))),
            new SimpleKeycloakToolHandler<>("create_identity_provider_mapper", "Create an identity provider mapper", MapperCreateRequest.class,
                (request, context) -> executionTemplate.execute("create_identity_provider_mapper", request.realm(), true, request,
                    () -> service.createMapper(request.realm(), request.alias(), request.payload()))),
            new SimpleKeycloakToolHandler<>("update_identity_provider_mapper", "Update an identity provider mapper", MapperUpdateRequest.class,
                (request, context) -> executionTemplate.execute("update_identity_provider_mapper", request.realm(), true, request,
                    () -> service.updateMapper(request.realm(), request.alias(), request.mapperId(), request.payload()))),
            new SimpleKeycloakToolHandler<>("delete_identity_provider_mapper", "Delete an identity provider mapper", MapperDeleteRequest.class,
                (request, context) -> executionTemplate.execute("delete_identity_provider_mapper", request.realm(), true, request,
                    () -> service.deleteMapper(request.realm(), request.alias(), request.mapperId())))
        );
    }

    @Override
    public Collection<? extends KeycloakToolHandler<?>> handlers() {
        return handlers;
    }

    public record RealmRequest(@NotBlank String realm) { }
    public record IdentityProviderLookupRequest(@NotBlank String realm, @NotBlank String alias) { }
    public record IdentityProviderPayloadRequest(@NotBlank String realm, @NotNull Map<String, Object> payload) { }
    public record IdentityProviderUpdateRequest(@NotBlank String realm, @NotBlank String alias, @NotNull Map<String, Object> payload) { }
    public record MapperCreateRequest(@NotBlank String realm, @NotBlank String alias, @NotNull Map<String, Object> payload) { }
    public record MapperUpdateRequest(@NotBlank String realm, @NotBlank String alias, @NotBlank String mapperId, @NotNull Map<String, Object> payload) { }
    public record MapperDeleteRequest(@NotBlank String realm, @NotBlank String alias, @NotBlank String mapperId) { }
}
