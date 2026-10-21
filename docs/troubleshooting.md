# Troubleshooting Guide

## Authentication Failures

- Verify the selected grant type matches the configured credentials.
- Confirm the Keycloak admin client or admin account has sufficient privileges.

## Tool Authorization Failures

- Check `mcp.security.tool-roles` mappings.
- Check whether the tool is listed under dangerous tools but not allow-listed.

## Connectivity Issues

- Verify `/actuator/health` and `/actuator/prometheus` are reachable.
- Check `KEYCLOAK_BASE_URL` and the token endpoint.

## MCP Client Issues

- For STDIO clients, use the packaged jar with the `stdio` profile (see client-setup.md).
- For HTTP clients, confirm the streamable transport endpoint is reachable.
