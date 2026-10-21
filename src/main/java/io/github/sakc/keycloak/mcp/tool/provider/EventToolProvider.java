package io.github.sakc.keycloak.mcp.tool.provider;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import io.github.sakc.keycloak.mcp.domain.service.EventAdminService;
import io.github.sakc.keycloak.mcp.domain.service.ToolExecutionTemplate;
import io.github.sakc.keycloak.mcp.tool.KeycloakToolHandler;
import io.github.sakc.keycloak.mcp.tool.KeycloakToolHandlerProvider;
import io.github.sakc.keycloak.mcp.tool.SimpleKeycloakToolHandler;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.stereotype.Component;

@Component
public class EventToolProvider implements KeycloakToolHandlerProvider {

    private final List<KeycloakToolHandler<?>> handlers;

    public EventToolProvider(EventAdminService service, ToolExecutionTemplate executionTemplate) {
        this.handlers = List.of(
            new SimpleKeycloakToolHandler<>("query_events", "Query authentication events", EventsQueryRequest.class,
                (request, context) -> executionTemplate.execute("query_events", request.realm(), false, request,
                    () -> service.queryEvents(request.realm(), request.query()))),
            new SimpleKeycloakToolHandler<>("query_admin_events", "Query admin events", EventsQueryRequest.class,
                (request, context) -> executionTemplate.execute("query_admin_events", request.realm(), false, request,
                    () -> service.queryAdminEvents(request.realm(), request.query()))),
            new SimpleKeycloakToolHandler<>("update_event_config", "Update realm event configuration", EventConfigRequest.class,
                (request, context) -> executionTemplate.execute("update_event_config", request.realm(), true, request,
                    () -> service.updateEventConfig(request.realm(), request.payload()))),
            new SimpleKeycloakToolHandler<>("clear_events", "Clear authentication events", RealmRequest.class,
                (request, context) -> executionTemplate.execute("clear_events", request.realm(), true, request,
                    () -> service.clearEvents(request.realm()))),
            new SimpleKeycloakToolHandler<>("clear_admin_events", "Clear admin events", RealmRequest.class,
                (request, context) -> executionTemplate.execute("clear_admin_events", request.realm(), true, request,
                    () -> service.clearAdminEvents(request.realm())))
        );
    }

    @Override
    public Collection<? extends KeycloakToolHandler<?>> handlers() {
        return handlers;
    }

    public record RealmRequest(@NotBlank String realm) { }
    public record EventsQueryRequest(@NotBlank String realm, @NotNull Map<String, Object> query) { }
    public record EventConfigRequest(@NotBlank String realm, @NotNull Map<String, Object> payload) { }
}
