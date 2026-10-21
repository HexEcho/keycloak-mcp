# Troubleshooting Guide

## Reading Logs

The server writes one JSON object per log event to the process console (`stderr` with
the `stdio` profile; otherwise the configured container/process console). The common
fields follow Elastic Common Schema (ECS): `@timestamp`, `log.level`, `log.logger`,
`message`, and `service.name`. Tool invocation events additionally include:

| ECS field | Meaning |
| --- | --- |
| `event.action` | Tool name |
| `event.category` | `iam` |
| `event.type` | `access` for read operations or `change` for mutating operations |
| `event.outcome` | `success` or `failure` |
| `event.duration` | Elapsed time in nanoseconds |
| `event.code` | Stable application error code, when applicable |
| `error.type` | Exception class, when applicable |

Search for `event.action` and `event.outcome` to identify a failing tool. Use
`event.code` and `error.type` to distinguish failures without depending on
unstructured exception messages. Tool outcome events omit request/response bodies,
realm and principal values, client identifiers, correlation/trace IDs, credentials,
and exception messages or stack traces. The encoder also masks common sensitive
fields and credential patterns in other log messages. A failure returned to the MCP
caller may contain more diagnostic detail than the log event; review it for
sensitive data before copying it into shared logs or support tickets.

## Authentication Failures

- Verify the selected grant type matches the configured credentials.
- Confirm the Keycloak admin client or admin account has sufficient privileges.
- Check for `event.action` and `event.code` values indicating authentication failures.

## Tool Authorization Failures

- Check `mcp.security.tool-roles` mappings.
- Check whether the tool is listed under dangerous tools but not allow-listed.
- Check for failed tool events and inspect the stable `event.code`; denied requests
  are logged without the caller's identity.

## Connectivity Issues

- Verify `/actuator/health` and `/actuator/prometheus` are reachable.
- Check `KEYCLOAK_BASE_URL` and the token endpoint.
- Look for `keycloak_client_error` or `keycloak_server_error` events and compare
  failure counts and latency in the Prometheus metrics. The application does not log
  raw Keycloak response bodies.

## MCP Client Issues

- For STDIO clients, use the packaged jar with the `stdio` profile (see client-setup.md).
- For HTTP clients, confirm the streamable transport endpoint is reachable.
- In STDIO mode, read the process `stderr` stream; `stdout` is reserved for MCP
  protocol messages.
