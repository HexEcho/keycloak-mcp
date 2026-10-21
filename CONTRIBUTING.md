# Contributing to Keycloak MCP

Thanks for your interest in contributing! This guide explains how to get set up and propose changes.

## Ways to contribute

- Report bugs and request features by opening an issue.
- Add or improve Keycloak tools (see [Extension Guide](docs/extension-guide.md)).
- Improve documentation, including client setup guides in [docs/client-setup.md](docs/client-setup.md).
- Add tests.

For security vulnerabilities, do **not** open a public issue; contact the maintainers privately.

## Development setup

Requirements: Java 21, Maven 3.9+, and optionally Docker (for a local Keycloak).

```bash
git clone <your-fork-url>
cd keycloak-mcp
docker compose up -d keycloak          # Keycloak on http://localhost:8081 (admin/admin, dev only)
mvn verify                             # build + tests
mvn spring-boot:run                    # HTTP MCP endpoint on http://localhost:8080/mcp
```

Optional live check against a running server: `./scripts/mcp-functional-regression.sh`.
To try the server from an AI client, see the [Client Setup Guide](docs/client-setup.md).

## Project layout

Base package: `io.github.sakc.keycloak.mcp`

- `api`, `config`: REST endpoints and Spring configuration
- `domain`: services, security, errors, observability
- `tool.provider`: MCP tool providers, one per Keycloak domain
- `mcp`: MCP wiring

## Adding a tool

1. Add the service method in `domain.service` using `KeycloakAdminClient`.
2. Add a handler in a `KeycloakToolHandlerProvider` under `tool.provider`, using a typed request record.
3. For read tools, register default and selectable response fields in `ResponseFieldCatalog`.
4. Add unit/integration tests.
5. Update [docs/tool-catalog.md](docs/tool-catalog.md) and [docs/tool-schemas.md](docs/tool-schemas.md).

## Pull request workflow

1. Fork the repo and create a branch from `main` (e.g. `feature/add-session-tools`, `fix/token-refresh`).
2. Keep changes focused; one logical change per PR.
3. Make sure `mvn verify` passes; CI runs it on every PR.
4. Update documentation for any user-visible change.
5. Open a PR describing what changed and why, and link related issues.

### Commit messages

Use short, imperative subjects, preferably [Conventional Commits](https://www.conventionalcommits.org/), e.g. `feat: add list_sessions tool` or `fix: handle expired admin token`.

## Coding guidelines

- Java 21, Spring Boot conventions; prefer typed records for request payloads.
- Never log or return secrets, tokens, or passwords.
- Mutating tools must go through `ToolExecutionTemplate` so validation, auditing, rate limiting, and error handling apply.
- Keep read-tool responses minimal by default; extra attributes are opt-in via `fields`.
- Add tests for new behavior; don't commit credentials or local config (`.env`, `.vscode/mcp.json`).

## License

By contributing, you agree that your contributions are licensed under the [MIT License](LICENSE).
