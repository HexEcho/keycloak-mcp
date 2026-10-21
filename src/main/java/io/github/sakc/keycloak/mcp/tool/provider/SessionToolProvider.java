package io.github.sakc.keycloak.mcp.tool.provider;

import java.util.Collection;
import java.util.List;

import io.github.sakc.keycloak.mcp.domain.service.SessionAdminService;
import io.github.sakc.keycloak.mcp.domain.service.ToolExecutionTemplate;
import io.github.sakc.keycloak.mcp.tool.KeycloakToolHandler;
import io.github.sakc.keycloak.mcp.tool.KeycloakToolHandlerProvider;
import io.github.sakc.keycloak.mcp.tool.SimpleKeycloakToolHandler;
import jakarta.validation.constraints.NotBlank;

import org.springframework.stereotype.Component;

@Component
public class SessionToolProvider implements KeycloakToolHandlerProvider {

    private final List<KeycloakToolHandler<?>> handlers;

    public SessionToolProvider(SessionAdminService service, ToolExecutionTemplate executionTemplate) {
        this.handlers = List.of(
            new SimpleKeycloakToolHandler<>("list_active_sessions", "List active sessions for a client", ClientRequest.class,
                (request, context) -> executionTemplate.execute("list_active_sessions", request.realm(), false, request,
                    () -> service.listActiveSessions(request.realm(), request.clientUuid()))),
            new SimpleKeycloakToolHandler<>("list_offline_sessions", "List offline sessions for a client", ClientRequest.class,
                (request, context) -> executionTemplate.execute("list_offline_sessions", request.realm(), false, request,
                    () -> service.listOfflineSessions(request.realm(), request.clientUuid()))),
            new SimpleKeycloakToolHandler<>("session_statistics", "Get realm session statistics", RealmRequest.class,
                (request, context) -> executionTemplate.execute("session_statistics", request.realm(), false, request,
                    () -> service.sessionStatistics(request.realm()))),
            new SimpleKeycloakToolHandler<>("list_sessions", "List realm session statistics and active session counters", RealmRequest.class,
                (request, context) -> executionTemplate.execute("list_sessions", request.realm(), false, request,
                    () -> service.sessionStatistics(request.realm()))),
            new SimpleKeycloakToolHandler<>("terminate_session", "Terminate a Keycloak session by id", SessionTerminateRequest.class,
                (request, context) -> executionTemplate.execute("terminate_session", request.realm(), true, request,
                    () -> service.terminateSession(request.realm(), request.sessionId()))),
            new SimpleKeycloakToolHandler<>("logout_all_sessions", "Logout all sessions in a realm", RealmRequest.class,
                (request, context) -> executionTemplate.execute("logout_all_sessions", request.realm(), true, request,
                    () -> service.logoutAll(request.realm())))
        );
    }

    @Override
    public Collection<? extends KeycloakToolHandler<?>> handlers() {
        return handlers;
    }

    public record RealmRequest(@NotBlank String realm) { }
    public record ClientRequest(@NotBlank String realm, @NotBlank String clientUuid) { }
    public record SessionTerminateRequest(@NotBlank String realm, @NotBlank String sessionId) { }
}
