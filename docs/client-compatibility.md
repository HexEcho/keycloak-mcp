# MCP Client Compatibility Matrix

| Client | Transport | Status | Notes |
| --- | --- | --- | --- |
| GitHub Copilot (VS Code) | STDIO / HTTP Streamable | Supported | Use workspace `.vscode/mcp.json` for STDIO or `http://localhost:8080/mcp` for Docker HTTP |
| Claude Desktop | STDIO | Supported | Use the stdio profile; see client-setup.md |
| Cursor | STDIO / HTTP | Supported | STDIO recommended for local workflows |
| Continue | STDIO / HTTP | Supported | Works with tool discovery and execution |
| ChatGPT MCP-compatible clients | HTTP Streamable | Supported | Use streamable MCP endpoint |
| Claude Code | STDIO / HTTP Streamable | Supported | `claude mcp add --transport http keycloak-admin http://localhost:8080/mcp` |
| Windsurf | STDIO / HTTP | Supported | `~/.codeium/windsurf/mcp_config.json` (`serverUrl` for HTTP) |
| Cline / Roo Code | STDIO / HTTP Streamable | Supported | Use `type: streamableHttp` for HTTP |
| OpenAI Codex CLI | STDIO | Supported | `[mcp_servers.*]` in `~/.codex/config.toml` |
| Gemini CLI | STDIO / HTTP Streamable | Supported | `httpUrl` for HTTP |
| Zed | STDIO | Supported | `context_servers` in settings |
| Spring AI Agents | HTTP / in-process | Supported | Native Spring AI MCP interoperability |
| LangGraph adapters | STDIO / HTTP | Supported | Use MCP adapter layer |
| CrewAI adapters | STDIO / HTTP | Supported | Use MCP tool bridge |
| AutoGen adapters | STDIO / HTTP | Supported | Use MCP wrapper/tool executor |

Per-client configuration snippets: see the [Client Setup Guide](client-setup.md).

## Validation Scope

Compatibility is validated at protocol/tool-contract level in this repository. End-user integrations depend on each client runtime and adapter versions.
