# Architecture

## Clean Architecture

```mermaid
flowchart LR
    MCP[MCP Tool Invocation] --> Handler[Tool Handler]
    Handler --> Template[ToolExecutionTemplate]
    Template --> Service[Application Service]
    Service --> Client[KeycloakAdminClient]
    Client --> API[Keycloak Admin API v2]
```

## Sequence

```mermaid
sequenceDiagram
    participant Client as MCP Client
    participant Server as Spring AI MCP Server
    participant Tool as Tool Handler
    participant Service as Domain Service
    participant KC as Keycloak Admin API
    Client->>Server: callTool(create_user)
    Server->>Tool: validate + authorize
    Tool->>Service: createUser(realm, payload)
    Service->>KC: POST /admin/realms/{realm}/users
    KC-->>Service: 201 Created
    Service-->>Tool: structured result
    Tool-->>Server: ToolResponse
    Server-->>Client: MCP tool result
```

## Design Notes

- Tools are registered through provider beans, not hard-coded in one class.
- Shared authorization, validation, rate limiting, audit logging, tracing, and response shaping live in `ToolExecutionTemplate`.
- New Keycloak domains can be added by implementing a service plus a provider.
