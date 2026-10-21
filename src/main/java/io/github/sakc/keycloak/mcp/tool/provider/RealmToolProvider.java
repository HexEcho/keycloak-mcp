package io.github.sakc.keycloak.mcp.tool.provider;

import java.util.Collection;
import java.util.List;

import io.github.sakc.keycloak.mcp.domain.dto.realm.RealmRepresentation;
import io.github.sakc.keycloak.mcp.domain.service.RealmAdminService;
import io.github.sakc.keycloak.mcp.domain.service.ToolExecutionTemplate;
import io.github.sakc.keycloak.mcp.tool.KeycloakToolHandler;
import io.github.sakc.keycloak.mcp.tool.KeycloakToolHandlerProvider;
import io.github.sakc.keycloak.mcp.tool.SimpleKeycloakToolHandler;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.stereotype.Component;

@Component
public class RealmToolProvider implements KeycloakToolHandlerProvider {

    private final List<KeycloakToolHandler<?>> handlers;

    public RealmToolProvider(RealmAdminService service, ToolExecutionTemplate executionTemplate) {
        this.handlers = List.of(
            new SimpleKeycloakToolHandler<>("list_realms", "List Keycloak realms", EmptyRealmRequest.class,
                (request, context) -> executionTemplate.execute("list_realms", null, false, request, service::listRealms)),
            new SimpleKeycloakToolHandler<>("get_realm", "Get a Keycloak realm by name", RealmLookupRequest.class,
                (request, context) -> executionTemplate.execute("get_realm", request.realm(), false, request,
                    () -> service.getRealm(request.realm()))),
            new SimpleKeycloakToolHandler<>("create_realm", "Create a Keycloak realm", RealmPayloadRequest.class,
                (request, context) -> executionTemplate.execute("create_realm", request.payload().realm(), true, request,
                    () -> service.createRealm(request.payload().toRepresentation()))),
            new SimpleKeycloakToolHandler<>("update_realm", "Update a Keycloak realm", RealmPayloadNamedRequest.class,
                (request, context) -> executionTemplate.execute("update_realm", request.realm(), true, request,
                    () -> service.updateRealm(request.realm(), request.payload().toRepresentation(request.realm())))),
            new SimpleKeycloakToolHandler<>("delete_realm", "Delete a Keycloak realm", RealmLookupRequest.class,
                (request, context) -> executionTemplate.execute("delete_realm", request.realm(), true, request,
                    () -> service.deleteRealm(request.realm()))),
            new SimpleKeycloakToolHandler<>("get_authentication_flows", "List authentication flows in a Keycloak realm", RealmLookupRequest.class,
                (request, context) -> executionTemplate.execute("get_authentication_flows", request.realm(), false, request,
                    () -> service.authenticationFlows(request.realm()))),
            new SimpleKeycloakToolHandler<>("update_realm_security_settings", "Update realm security settings such as brute force protection or password policy", RealmPayloadNamedRequest.class,
                (request, context) -> executionTemplate.execute("update_realm_security_settings", request.realm(), true, request,
                    () -> service.updateRealmSecurity(request.realm(), request.payload().toRepresentation(request.realm())))),
            new SimpleKeycloakToolHandler<>("update_login_settings", "Update realm login settings, SMTP, themes, and attributes", RealmPayloadNamedRequest.class,
                (request, context) -> executionTemplate.execute("update_login_settings", request.realm(), true, request,
                    () -> service.updateLoginSettings(request.realm(), request.payload().toRepresentation(request.realm()))))
        );
    }

    @Override
    public Collection<? extends KeycloakToolHandler<?>> handlers() {
        return handlers;
    }

    public record EmptyRealmRequest() { }

    public record RealmLookupRequest(@NotBlank String realm) { }

    public record RealmPayloadRequest(@NotNull @Valid CreateRealmPayload payload) { }

    public record CreateRealmPayload(
        @NotBlank String realm,
        Boolean enabled,
        Boolean registrationAllowed,
        Boolean resetPasswordAllowed,
        Boolean rememberMe,
        Boolean verifyEmail,
        Boolean bruteForceProtected,
        String passwordPolicy,
        String loginTheme,
        String accountTheme,
        String adminTheme,
        String emailTheme,
        java.util.Map<String, String> smtpServer,
        java.util.Map<String, String> attributes
    ) {
        public RealmRepresentation toRepresentation() {
            return new RealmRepresentation(null, realm, enabled, registrationAllowed, resetPasswordAllowed, rememberMe,
                verifyEmail, bruteForceProtected, passwordPolicy, loginTheme, accountTheme, adminTheme, emailTheme, smtpServer, attributes);
        }
    }

    public record RealmPayloadNamedRequest(@NotBlank String realm, @NotNull @Valid UpdateRealmPayload payload) { }

    public record UpdateRealmPayload(
        Boolean enabled,
        Boolean registrationAllowed,
        Boolean resetPasswordAllowed,
        Boolean rememberMe,
        Boolean verifyEmail,
        Boolean bruteForceProtected,
        String passwordPolicy,
        String loginTheme,
        String accountTheme,
        String adminTheme,
        String emailTheme,
        java.util.Map<String, String> smtpServer,
        java.util.Map<String, String> attributes
    ) {
        public RealmRepresentation toRepresentation(String realm) {
            return new RealmRepresentation(null, realm, enabled, registrationAllowed, resetPasswordAllowed, rememberMe,
                verifyEmail, bruteForceProtected, passwordPolicy, loginTheme, accountTheme, adminTheme, emailTheme, smtpServer, attributes);
        }
    }
}
