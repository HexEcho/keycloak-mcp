# Client Setup Guide

How to connect AI agents and IDE plugins to the Keycloak MCP server, and how to use the minimal-response `fields` feature.

## 1. Choose a transport

| Mode | When to use | Endpoint / command |
| --- | --- | --- |
| STDIO | Local development; the client launches the server | `java -jar target/keycloak-mcp-0.1.0-SNAPSHOT.jar --spring.profiles.active=stdio` |
| Streamable HTTP | Shared/remote server, Docker, Kubernetes | `http://localhost:8080/mcp` |

Build the jar once for STDIO (faster than `mvn spring-boot:run` and avoids Maven output on stdout):

```bash
mvn -q -DskipTests package
```

For HTTP, start the server with `docker compose up --build` (MCP on `http://localhost:8080/mcp`, Keycloak on `http://localhost:8081`) or `mvn spring-boot:run`.

> STDIO requires an absolute path to the jar. Replace `/ABS/PATH/TO` in all examples below.

### Environment variables

| Variable | Example | Purpose |
| --- | --- | --- |
| `KEYCLOAK_BASE_URL` | `http://localhost:8081` | Keycloak URL |
| `KEYCLOAK_ADMIN_REALM` | `master` | Realm used for the admin token |
| `KEYCLOAK_AUTH_MODE` | `PASSWORD` or `CLIENT_CREDENTIALS` | How the server authenticates |
| `KEYCLOAK_CLIENT_ID` / `KEYCLOAK_CLIENT_SECRET` | `admin-cli` / secret | Client credentials |
| `KEYCLOAK_USERNAME` / `KEYCLOAK_PASSWORD` | `admin` / `admin` | Only for `PASSWORD` mode (dev only) |

Never commit real secrets; use your client's env/secret-input mechanism. If the HTTP server enforces JWT (`MCP_ENFORCE_HTTP_JWT=true`, `enterprise` profile), clients must send `Authorization: Bearer <token>`.

## 2. Client configurations

### GitHub Copilot (VS Code)

Workspace file `.vscode/mcp.json` (start from `.vscode/mcp.json.example`):

```json
{
  "servers": {
    "keycloak-admin-mcp": {
      "type": "stdio",
      "command": "java",
      "args": ["-jar", "/ABS/PATH/TO/target/keycloak-mcp-0.1.0-SNAPSHOT.jar", "--spring.profiles.active=stdio"],
      "env": {
        "KEYCLOAK_BASE_URL": "http://localhost:8081",
        "KEYCLOAK_AUTH_MODE": "PASSWORD",
        "KEYCLOAK_CLIENT_ID": "admin-cli",
        "KEYCLOAK_USERNAME": "admin",
        "KEYCLOAK_PASSWORD": "admin"
      }
    },
    "keycloak-admin-mcp-http": { "type": "http", "url": "http://localhost:8080/mcp" }
  }
}
```

Run **MCP: List Servers** to start/trust the server, then use Agent mode and enable the tools in the tool picker.

### Cursor

`.cursor/mcp.json` (project) or `~/.cursor/mcp.json` (global):

```json
{
  "mcpServers": {
    "keycloak-admin": {
      "command": "java",
      "args": ["-jar", "/ABS/PATH/TO/target/keycloak-mcp-0.1.0-SNAPSHOT.jar", "--spring.profiles.active=stdio"],
      "env": {
        "KEYCLOAK_BASE_URL": "http://localhost:8081",
        "KEYCLOAK_AUTH_MODE": "PASSWORD",
        "KEYCLOAK_CLIENT_ID": "admin-cli",
        "KEYCLOAK_USERNAME": "admin",
        "KEYCLOAK_PASSWORD": "admin"
      }
    }
  }
}
```

HTTP variant: `{ "mcpServers": { "keycloak-admin": { "url": "http://localhost:8080/mcp", "headers": { "Authorization": "Bearer ${env:MCP_TOKEN}" } } } }`.
Enable it under *Settings → Cursor Settings → MCP* and use it from Agent mode.

### Continue (VS Code / JetBrains)

Create `.continue/mcpServers/keycloak.yaml` in the workspace (MCP tools work in Agent mode only):

```yaml
name: Keycloak MCP
version: 0.0.1
schema: v1
mcpServers:
  - name: keycloak-admin
    type: stdio
    command: java
    args:
      - -jar
      - /ABS/PATH/TO/target/keycloak-mcp-0.1.0-SNAPSHOT.jar
      - --spring.profiles.active=stdio
    env:
      KEYCLOAK_BASE_URL: http://localhost:8081
      KEYCLOAK_AUTH_MODE: PASSWORD
      KEYCLOAK_CLIENT_ID: admin-cli
      KEYCLOAK_USERNAME: admin
      KEYCLOAK_PASSWORD: admin
```

HTTP variant:

```yaml
mcpServers:
  - name: keycloak-admin-http
    type: streamable-http
    url: http://localhost:8080/mcp
```

Continue also accepts JSON files dropped into `.continue/mcpServers/` using the Claude/Cursor `mcpServers` format.

### Claude Desktop

`~/Library/Application Support/Claude/claude_desktop_config.json` (macOS) or `%APPDATA%\Claude\claude_desktop_config.json` (Windows). Use the same `mcpServers` block as Cursor (STDIO). For a remote HTTP server, bridge with `mcp-remote`:

```json
{
  "mcpServers": {
    "keycloak-admin": {
      "command": "npx",
      "args": ["-y", "mcp-remote", "http://localhost:8080/mcp", "--allow-http"]
    }
  }
}
```

Restart Claude Desktop after editing.

### Claude Code

```bash
claude mcp add --transport http keycloak-admin http://localhost:8080/mcp
# or STDIO
claude mcp add keycloak-admin \
  -e KEYCLOAK_BASE_URL=http://localhost:8081 -e KEYCLOAK_AUTH_MODE=PASSWORD \
  -e KEYCLOAK_USERNAME=admin -e KEYCLOAK_PASSWORD=admin \
  -- java -jar /ABS/PATH/TO/target/keycloak-mcp-0.1.0-SNAPSHOT.jar --spring.profiles.active=stdio
```

### Windsurf

`~/.codeium/windsurf/mcp_config.json` — same `mcpServers` STDIO block as Cursor; for HTTP use `"serverUrl": "http://localhost:8080/mcp"`.

### Cline / Roo Code (VS Code)

*MCP Servers → Configure* (`cline_mcp_settings.json`): STDIO uses the Cursor block; HTTP uses
`{ "mcpServers": { "keycloak-admin": { "type": "streamableHttp", "url": "http://localhost:8080/mcp" } } }`.

### OpenAI Codex CLI

`~/.codex/config.toml`:

```toml
[mcp_servers.keycloak-admin]
command = "java"
args = ["-jar", "/ABS/PATH/TO/target/keycloak-mcp-0.1.0-SNAPSHOT.jar", "--spring.profiles.active=stdio"]

[mcp_servers.keycloak-admin.env]
KEYCLOAK_BASE_URL = "http://localhost:8081"
KEYCLOAK_AUTH_MODE = "PASSWORD"
KEYCLOAK_USERNAME = "admin"
KEYCLOAK_PASSWORD = "admin"
```

### Gemini CLI

`~/.gemini/settings.json`: Cursor-style `mcpServers` for STDIO, or `{ "mcpServers": { "keycloak-admin": { "httpUrl": "http://localhost:8080/mcp" } } }` for HTTP.

### Zed

`settings.json`:

```json
{
  "context_servers": {
    "keycloak-admin": {
      "command": { "path": "java", "args": ["-jar", "/ABS/PATH/TO/target/keycloak-mcp-0.1.0-SNAPSHOT.jar", "--spring.profiles.active=stdio"], "env": { "KEYCLOAK_BASE_URL": "http://localhost:8081" } }
    }
  }
}
```

### Spring AI client / custom agents

```yaml
spring.ai.mcp.client.streamable-http.connections.keycloak.url: http://localhost:8080
spring.ai.mcp.client.streamable-http.connections.keycloak.endpoint: /mcp
```

Any MCP SDK (TypeScript, Python, Java) can connect to `http://localhost:8080/mcp` with the Streamable HTTP transport. For browser-based tools such as MCP Inspector, add its origin (e.g. `http://localhost:6274`) to `MCP_STREAMABLE_HTTP_ALLOWED_ORIGINS`:

```bash
npx @modelcontextprotocol/inspector
```

## 3. Using the tools: minimal responses and `fields`

Read tools (`list_*`, `search_*`, `get_*`, `users_with_role`, `groups_with_role`, `query_*events`) return only a small default set of attributes to save tokens. Recommended agent workflow:

1. **Discover** — call `describe_response_fields` (optionally `{"tool":"list_clients"}`) to see each read tool's default and selectable fields.
2. **Narrow** — call the read tool with `fields` to add what you need:
   `{"realm":"demo","fields":["clientId","publicClient","protocol"]}`
   (array or comma-separated string; dotted paths like `attributes.foo` work).
3. **Page** — use `first` / `max` for large lists.
4. **Full detail only when needed** — `{"fields":["*"]}` returns the complete Keycloak representation.

Example prompts:

- "List the clients in realm `demo` and show whether each is public."
- "Find users in realm `demo` matching `alice` and show email and enabled status."
- "Which fields can I request for `list_users`?"

## 4. Troubleshooting

- **STDIO client reports invalid JSON / fails to connect**: stdout must contain only protocol messages. Use the packaged jar with the `stdio` profile (the profile routes logs to stderr). Make sure no wrapper script or `mvn` output writes to stdout.
- **No tools shown**: restart the client, confirm Agent mode is on, and check `curl http://localhost:8080/api/tools/catalog` (HTTP mode).
- **403/Origin or Host rejected (HTTP)**: add the client's origin/host to `MCP_STREAMABLE_HTTP_ALLOWED_ORIGINS` / `MCP_STREAMABLE_HTTP_ALLOWED_HOSTS`.
- **401 (HTTP)**: JWT enforcement is on; supply a bearer token or disable `MCP_ENFORCE_HTTP_JWT` for local use.
- **Keycloak auth errors**: verify `KEYCLOAK_BASE_URL`, auth mode, and credentials; see [Troubleshooting](troubleshooting.md).
