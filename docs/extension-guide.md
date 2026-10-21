# Extension Guide

## Add a New Keycloak API Domain

1. Create a new domain service under `domain.service`.
2. Implement Keycloak REST calls through `KeycloakAdminClient`.
3. Add a new provider under `tool.provider` implementing `KeycloakToolHandlerProvider`.
4. Register tool names, descriptions, and request records.
5. Add unit and integration tests.
6. Update `docs/tool-catalog.md`.

## Version Upgrades

When Keycloak adds or changes admin endpoints, prefer adding a new service method and provider handler rather than editing shared framework code.
