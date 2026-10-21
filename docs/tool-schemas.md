# Tool Schema Guide

This server uses strongly typed Java records for key mutation payloads. MCP clients receive JSON schemas generated from tool request types.

## Typed Payloads Introduced

- create_user
- update_user
- reset_password
- assign_realm_roles
- assign_client_roles
- create_client
- update_client
- create_protocol_mapper
- update_protocol_mapper
- create_realm
- update_realm

## Example: create_user

```json
{
  "realm": "demo",
  "payload": {
    "username": "jane",
    "email": "jane@example.com",
    "enabled": true,
    "attributes": {
      "department": ["engineering"]
    }
  }
}
```

## Example: update_user

```json
{
  "realm": "demo",
  "userId": "4f5d4f31-2d4a-456d-b4be-937f50d4d419",
  "payload": {
    "firstName": "Jane",
    "lastName": "Doe",
    "enabled": true
  }
}
```

## Example: reset_password

```json
{
  "realm": "demo",
  "userId": "4f5d4f31-2d4a-456d-b4be-937f50d4d419",
  "password": {
    "type": "password",
    "value": "S3cur3P@ssw0rd",
    "temporary": false
  }
}
```

  ## Example: create_client

  ```json
  {
    "realm": "demo",
    "payload": {
      "clientId": "demo-app",
      "name": "Demo App",
      "enabled": true,
      "publicClient": false,
      "serviceAccountsEnabled": true,
      "redirectUris": ["https://demo.example.com/callback"],
      "webOrigins": ["https://demo.example.com"],
      "attributes": {
        "access.token.lifespan": "300"
      }
    }
  }
  ```

  ## Example: create_realm

  ```json
  {
    "payload": {
      "realm": "customer-portal",
      "enabled": true,
      "registrationAllowed": false,
      "verifyEmail": true,
      "passwordPolicy": "length(12) and digits(1) and upperCase(1)",
      "loginTheme": "keycloak"
    }
  }
  ```

## Notes

- Additional tools still support map-based payloads where Keycloak APIs are highly dynamic.
- The recommended pattern for future tools is typed records first, map payloads only where necessary.

## Minimal responses and the `fields` argument

Read tools (`list_*`, `search_*`, `get_*`, `users_with_role`, `groups_with_role`, `query_*events`) return only a minimal
set of attributes by default, e.g. clients return `id`, `clientId`, `name`, `description`; users return
`id`, `username`, `email`, `enabled`.

Pass `fields` (array or comma-separated string) to request more, e.g. `{"realm":"demo","clientUuid":"...","fields":["clientId","publicClient","protocol"]}`.
Dotted paths such as `attributes.foo` are supported. Use `"fields":["*"]` to return the full Keycloak representation.
Use `first`/`max` to page through large lists.

Call `describe_response_fields` (optional `tool` argument, e.g. `{"tool":"list_clients"}`) to discover each read tool's default and selectable fields before choosing `fields`.
