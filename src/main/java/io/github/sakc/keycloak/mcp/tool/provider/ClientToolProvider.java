package io.github.sakc.keycloak.mcp.tool.provider;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import io.github.sakc.keycloak.mcp.domain.dto.client.ClientRepresentation;
import io.github.sakc.keycloak.mcp.domain.dto.client.ProtocolMapperRepresentation;
import io.github.sakc.keycloak.mcp.domain.service.ClientAdminService;
import io.github.sakc.keycloak.mcp.domain.service.ToolExecutionTemplate;
import io.github.sakc.keycloak.mcp.tool.KeycloakToolHandler;
import io.github.sakc.keycloak.mcp.tool.KeycloakToolHandlerProvider;
import io.github.sakc.keycloak.mcp.tool.SimpleKeycloakToolHandler;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import org.springframework.stereotype.Component;

@Component
public class ClientToolProvider implements KeycloakToolHandlerProvider {

    private final List<KeycloakToolHandler<?>> handlers;

    public ClientToolProvider(ClientAdminService service, ToolExecutionTemplate executionTemplate) {
        this.handlers = List.of(
            new SimpleKeycloakToolHandler<>("create_client", "Create a Keycloak client", ClientPayloadRequest.class,
                (request, context) -> executionTemplate.execute("create_client", request.realm(), true, request,
                    () -> service.createClient(request.realm(), request.payload().toRepresentation()))),
            new SimpleKeycloakToolHandler<>("update_client", "Update a Keycloak client", ClientNamedPayloadRequest.class,
                (request, context) -> executionTemplate.execute("update_client", request.realm(), true, request,
                    () -> service.updateClient(request.realm(), request.clientUuid(), request.payload().toRepresentation()))),
            new SimpleKeycloakToolHandler<>("delete_client", "Delete a Keycloak client", ClientLookupRequest.class,
                (request, context) -> executionTemplate.execute("delete_client", request.realm(), true, request,
                    () -> service.deleteClient(request.realm(), request.clientUuid()))),
            new SimpleKeycloakToolHandler<>("list_clients", "List Keycloak clients", SearchClientsRequest.class,
                (request, context) -> executionTemplate.execute("list_clients", request.realm(), false, request,
                    () -> service.listClients(request.realm(), request.toQueryMap()))),
            new SimpleKeycloakToolHandler<>("search_clients", "Search Keycloak clients", SearchClientsRequest.class,
                (request, context) -> executionTemplate.execute("search_clients", request.realm(), false, request,
                    () -> service.listClients(request.realm(), request.toQueryMap()))),
            new SimpleKeycloakToolHandler<>("get_client", "Get a Keycloak client by UUID", ClientLookupRequest.class,
                (request, context) -> executionTemplate.execute("get_client", request.realm(), false, request,
                    () -> service.getClient(request.realm(), request.clientUuid()))),
            new SimpleKeycloakToolHandler<>("rotate_client_secret", "Rotate a Keycloak client secret", ClientLookupRequest.class,
                (request, context) -> executionTemplate.execute("rotate_client_secret", request.realm(), true, request,
                    () -> service.rotateSecret(request.realm(), request.clientUuid()))),
            new SimpleKeycloakToolHandler<>("list_protocol_mappers", "List protocol mappers for a client", ClientLookupRequest.class,
                (request, context) -> executionTemplate.execute("list_protocol_mappers", request.realm(), false, request,
                    () -> service.listProtocolMappers(request.realm(), request.clientUuid()))),
            new SimpleKeycloakToolHandler<>("create_protocol_mapper", "Create a protocol mapper for a client", ProtocolMapperRequest.class,
                (request, context) -> executionTemplate.execute("create_protocol_mapper", request.realm(), true, request,
                    () -> service.createProtocolMapper(request.realm(), request.clientUuid(), request.mapper().toRepresentation()))),
            new SimpleKeycloakToolHandler<>("update_protocol_mapper", "Update a protocol mapper for a client", ProtocolMapperUpdateRequest.class,
                (request, context) -> executionTemplate.execute("update_protocol_mapper", request.realm(), true, request,
                    () -> service.updateProtocolMapper(request.realm(), request.clientUuid(), request.mapperId(), request.mapper().toRepresentation()))),
            new SimpleKeycloakToolHandler<>("delete_protocol_mapper", "Delete a protocol mapper for a client", ProtocolMapperDeleteRequest.class,
                (request, context) -> executionTemplate.execute("delete_protocol_mapper", request.realm(), true, request,
                    () -> service.deleteProtocolMapper(request.realm(), request.clientUuid(), request.mapperId()))),
            new SimpleKeycloakToolHandler<>("attach_default_client_scopes", "Attach default client scopes to a client", ClientScopesRequest.class,
                (request, context) -> executionTemplate.execute("attach_default_client_scopes", request.realm(), true, request,
                    () -> service.attachDefaultClientScopes(request.realm(), request.clientUuid(), request.scopeIds())))
        );
    }

    @Override
    public Collection<? extends KeycloakToolHandler<?>> handlers() {
        return handlers;
    }

    public record ClientPayloadRequest(@NotBlank String realm, @NotNull @Valid CreateClientPayload payload) { }

    public record CreateClientPayload(
        @NotBlank String clientId,
        String name,
        String description,
        String protocol,
        String rootUrl,
        String baseUrl,
        Boolean enabled,
        Boolean publicClient,
        Boolean bearerOnly,
        Boolean serviceAccountsEnabled,
        Boolean directAccessGrantsEnabled,
        List<String> redirectUris,
        List<String> webOrigins,
        Map<String, String> attributes
    ) {
        public ClientRepresentation toRepresentation() {
            return new ClientRepresentation(null, clientId, name, description, protocol, rootUrl, baseUrl, enabled, publicClient, bearerOnly,
                serviceAccountsEnabled, directAccessGrantsEnabled, redirectUris, webOrigins, attributes);
        }
    }

    public record ClientNamedPayloadRequest(@NotBlank String realm, @NotBlank String clientUuid, @NotNull @Valid UpdateClientPayload payload) { }

    public record UpdateClientPayload(
        String clientId,
        String name,
        String description,
        String protocol,
        String rootUrl,
        String baseUrl,
        Boolean enabled,
        Boolean publicClient,
        Boolean bearerOnly,
        Boolean serviceAccountsEnabled,
        Boolean directAccessGrantsEnabled,
        List<String> redirectUris,
        List<String> webOrigins,
        Map<String, String> attributes
    ) {
        public ClientRepresentation toRepresentation() {
            return new ClientRepresentation(null, clientId, name, description, protocol, rootUrl, baseUrl, enabled, publicClient, bearerOnly,
                serviceAccountsEnabled, directAccessGrantsEnabled, redirectUris, webOrigins, attributes);
        }
    }

    public record ClientLookupRequest(@NotBlank String realm, @NotBlank String clientUuid) { }

    public record SearchClientsRequest(@NotBlank String realm, String clientId, String search, Boolean viewableOnly, Integer first, Integer max) {
        public Map<String, Object> toQueryMap() {
            return Map.of(
                "clientId", clientId == null ? "" : clientId,
                "search", search == null ? "" : search,
                "viewableOnly", viewableOnly == null ? "" : viewableOnly,
                "first", first == null ? 0 : first,
                "max", max == null ? 50 : max
            );
        }
    }

    public record ProtocolMapperRequest(@NotBlank String realm, @NotBlank String clientUuid, @NotNull @Valid ProtocolMapperPayload mapper) { }

    public record ProtocolMapperUpdateRequest(@NotBlank String realm, @NotBlank String clientUuid, @NotBlank String mapperId, @NotNull @Valid ProtocolMapperPayload mapper) { }

    public record ProtocolMapperPayload(
        String name,
        String protocol,
        String protocolMapper,
        Boolean consentRequired,
        String consentText,
        Map<String, String> config
    ) {
        public ProtocolMapperRepresentation toRepresentation() {
            return new ProtocolMapperRepresentation(null, name, protocol, protocolMapper, consentRequired, consentText, config);
        }
    }

    public record ProtocolMapperDeleteRequest(@NotBlank String realm, @NotBlank String clientUuid, @NotBlank String mapperId) { }

    public record ClientScopesRequest(@NotBlank String realm, @NotBlank String clientUuid, @NotEmpty List<String> scopeIds) { }
}
