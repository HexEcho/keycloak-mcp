# Configuration Guide

## Core Properties

- `KEYCLOAK_BASE_URL`
- `KEYCLOAK_ADMIN_REALM`
- `KEYCLOAK_AUTH_MODE`
- `KEYCLOAK_CLIENT_ID`
- `KEYCLOAK_CLIENT_SECRET`
- `KEYCLOAK_USERNAME`
- `KEYCLOAK_PASSWORD`
- `MCP_ENFORCE_HTTP_JWT`
- `MCP_ALLOW_ANONYMOUS_STDIO`
- `MCP_STREAMABLE_HTTP_ENABLED`
- `MCP_STREAMABLE_HTTP_ENDPOINT`
- `MCP_STREAMABLE_HTTP_ALLOWED_ORIGINS`
- `MCP_STREAMABLE_HTTP_ALLOWED_HOSTS`
- `MCP_STREAMABLE_HTTP_REQUIRE_MTLS`
- `MCP_STREAMABLE_HTTP_MTLS_HEADER`

## Profiles

- `default`: HTTP streamable MCP server
- `docker`: container deployment profile
- `stdio`: process-based MCP for local AI clients
- `enterprise`: hardened overlay profile (JWT on, mTLS required, strict origin/host allow-lists)

Use enterprise profile locally:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=enterprise
```

## Streamable HTTP Enterprise Defaults

- Transport endpoint defaults to `/mcp`.
- Keep-alive defaults to `20s` for long-lived clients behind API gateways.
- Origin and host allow-lists are enforced at transport level before tool execution.
- Forwarded headers are honored (`server.forward-headers-strategy=framework`) for ingress and service mesh topologies.
- Optional mTLS forwarding is supported via `MCP_STREAMABLE_HTTP_REQUIRE_MTLS=true` and `MCP_STREAMABLE_HTTP_MTLS_HEADER`.

## Security Controls

- Dangerous tools are deny-by-default unless allowed in config.
- Tool-level role checks are configured under `mcp.security.tool-roles`.
- All mutating tools emit audit logs.
- Structured logs mask tokens, passwords, and client secrets.
