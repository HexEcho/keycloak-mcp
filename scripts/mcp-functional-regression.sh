#!/usr/bin/env bash
set -euo pipefail

MCP_URL="${MCP_URL:-http://localhost:8080/mcp}"
MCP_ORIGIN="${MCP_ORIGIN:-http://localhost:6274}"
MCP_PROTOCOL_VERSION="${MCP_PROTOCOL_VERSION:-2025-11-25}"

pass=0
fail=0

init_session() {
  local init_hdr init_body sid
  init_hdr=$(mktemp)
  init_body=$(mktemp)

  curl -sS -D "$init_hdr" -o "$init_body" -X POST "$MCP_URL" \
    -H 'Content-Type: application/json' \
    -H 'Accept: application/json, text/event-stream' \
    -H "Origin: $MCP_ORIGIN" \
    -H "MCP-Protocol-Version: $MCP_PROTOCOL_VERSION" \
    --data '{"jsonrpc":"2.0","id":"init","method":"initialize","params":{"protocolVersion":"2025-11-25","capabilities":{"roots":{"listChanged":true}},"clientInfo":{"name":"mcp-functional-regression","version":"1.0.0"}}}'

  sid=$(grep -i '^mcp-session-id:' "$init_hdr" | awk '{print $2}' | tr -d '\r')
  if [[ -z "$sid" ]]; then
    echo "Failed to initialize MCP session"
    cat "$init_hdr"
    cat "$init_body"
    exit 1
  fi
  echo "$sid"
}

call_tool() {
  local sid="$1"
  local name="$2"
  local args="$3"
  local out
  out=$(mktemp)

  curl -sS -o "$out" -X POST "$MCP_URL" \
    -H 'Content-Type: application/json' \
    -H 'Accept: application/json, text/event-stream' \
    -H "Origin: $MCP_ORIGIN" \
    -H "MCP-Protocol-Version: $MCP_PROTOCOL_VERSION" \
    -H "Mcp-Session-Id: $sid" \
    --data "{\"jsonrpc\":\"2.0\",\"id\":\"call-$name\",\"method\":\"tools/call\",\"params\":{\"name\":\"$name\",\"arguments\":$args}}"

  tr -d '\n' < "$out"
}

assert_success() {
  local name="$1"
  local response="$2"
  if echo "$response" | grep -q '"isError":false'; then
    echo "PASS $name"
    pass=$((pass + 1))
  else
    echo "FAIL $name"
    echo "$response"
    fail=$((fail + 1))
  fi
}

extract_escaped_field() {
  local key="$1"
  local text="$2"
  echo "$text" | sed -n "s/.*\\\\\"$key\\\\\":\\\\\"\([^\\\\\"]*\)\\\\\".*/\1/p" | head -n1
}

main() {
  local sid ts realm username client_id group_name role_name
  local user_id group_id client_uuid
  local response

  sid=$(init_session)
  echo "SESSION=$sid"

  ts=$(date +%s)
  realm="mcp-smoke-$ts"
  username="smoke-user-$ts"
  client_id="smoke-client-$ts"
  group_name="smoke-group-$ts"
  role_name="smoke-role-$ts"

  response=$(call_tool "$sid" create_realm "{\"payload\":{\"realm\":\"$realm\",\"enabled\":true,\"registrationAllowed\":false}}")
  assert_success create_realm "$response"

  response=$(call_tool "$sid" get_realm "{\"realm\":\"$realm\"}")
  assert_success get_realm "$response"

  response=$(call_tool "$sid" create_user "{\"realm\":\"$realm\",\"payload\":{\"username\":\"$username\",\"enabled\":true,\"email\":\"$username@example.com\"}}")
  assert_success create_user "$response"
  user_id=$(extract_escaped_field userId "$response")
  echo "USER_ID=${user_id:-}"

  response=$(call_tool "$sid" get_user "{\"realm\":\"$realm\",\"userId\":\"$user_id\"}")
  assert_success get_user "$response"

  response=$(call_tool "$sid" update_user "{\"realm\":\"$realm\",\"userId\":\"$user_id\",\"payload\":{\"firstName\":\"Smoke\",\"lastName\":\"User\"}}")
  assert_success update_user "$response"

  response=$(call_tool "$sid" reset_password "{\"realm\":\"$realm\",\"userId\":\"$user_id\",\"password\":{\"type\":\"password\",\"value\":\"Passw0rd!\",\"temporary\":false}}")
  assert_success reset_password "$response"

  response=$(call_tool "$sid" create_group "{\"realm\":\"$realm\",\"payload\":{\"name\":\"$group_name\"}}")
  assert_success create_group "$response"
  group_id=$(extract_escaped_field groupId "$response")
  echo "GROUP_ID=${group_id:-}"

  response=$(call_tool "$sid" add_group_member "{\"realm\":\"$realm\",\"groupId\":\"$group_id\",\"userId\":\"$user_id\"}")
  assert_success add_group_member "$response"

  response=$(call_tool "$sid" list_group_members "{\"realm\":\"$realm\",\"groupId\":\"$group_id\",\"first\":0,\"max\":20}")
  assert_success list_group_members "$response"

  response=$(call_tool "$sid" create_client "{\"realm\":\"$realm\",\"payload\":{\"clientId\":\"$client_id\",\"enabled\":true,\"publicClient\":true,\"redirectUris\":[\"http://localhost/*\"]}}")
  assert_success create_client "$response"
  client_uuid=$(extract_escaped_field clientUuid "$response")
  echo "CLIENT_UUID=${client_uuid:-}"

  response=$(call_tool "$sid" get_client "{\"realm\":\"$realm\",\"clientUuid\":\"$client_uuid\"}")
  assert_success get_client "$response"

  response=$(call_tool "$sid" rotate_client_secret "{\"realm\":\"$realm\",\"clientUuid\":\"$client_uuid\"}")
  assert_success rotate_client_secret "$response"

  response=$(call_tool "$sid" create_realm_role "{\"realm\":\"$realm\",\"payload\":{\"name\":\"$role_name\",\"description\":\"smoke role\"}}")
  assert_success create_realm_role "$response"

  response=$(call_tool "$sid" assign_realm_roles "{\"realm\":\"$realm\",\"userId\":\"$user_id\",\"roles\":[{\"name\":\"$role_name\"}]}")
  assert_success assign_realm_roles "$response"

  response=$(call_tool "$sid" users_with_role "{\"realm\":\"$realm\",\"roleName\":\"$role_name\"}")
  assert_success users_with_role "$response"

  response=$(call_tool "$sid" remove_group_member "{\"realm\":\"$realm\",\"groupId\":\"$group_id\",\"userId\":\"$user_id\"}")
  assert_success remove_group_member "$response"

  response=$(call_tool "$sid" delete_user "{\"realm\":\"$realm\",\"userId\":\"$user_id\"}")
  assert_success delete_user "$response"

  response=$(call_tool "$sid" delete_group "{\"realm\":\"$realm\",\"groupId\":\"$group_id\"}")
  assert_success delete_group "$response"

  response=$(call_tool "$sid" delete_client "{\"realm\":\"$realm\",\"clientUuid\":\"$client_uuid\"}")
  assert_success delete_client "$response"

  response=$(call_tool "$sid" delete_realm_role "{\"realm\":\"$realm\",\"roleName\":\"$role_name\"}")
  assert_success delete_realm_role "$response"

  response=$(call_tool "$sid" delete_realm "{\"realm\":\"$realm\"}")
  assert_success delete_realm "$response"

  echo "PASS_COUNT=$pass"
  echo "FAIL_COUNT=$fail"
  if [[ "$fail" -gt 0 ]]; then
    exit 2
  fi
}

main "$@"
