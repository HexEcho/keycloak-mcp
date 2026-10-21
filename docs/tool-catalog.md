# Tool Catalog

## Field Discovery

- `describe_response_fields` (default and selectable `fields` per read tool)

## User Management

- `create_user`
- `update_user`
- `delete_user`
- `search_users`
- `list_users`
- `get_user`
- `reset_password`
- `enable_user`
- `disable_user`
- `unlock_user`
- `logout_user_sessions`
- `send_verification_email`
- `execute_required_actions`
- `assign_realm_roles`
- `remove_realm_roles`
- `assign_client_roles`
- `remove_client_roles`
- `join_groups`
- `remove_from_groups`
- `update_user_attributes`
- `list_user_credentials`
- `list_federated_identities`

## Realm Management

- `list_realms`
- `get_realm`
- `create_realm`
- `update_realm`
- `delete_realm`
- `get_authentication_flows`
- `update_realm_security_settings`
- `update_login_settings`

## Client Management

- `create_client`
- `update_client`
- `delete_client`
- `list_clients`
- `search_clients`
- `get_client`
- `rotate_client_secret`
- `list_protocol_mappers`
- `create_protocol_mapper`
- `update_protocol_mapper`
- `delete_protocol_mapper`
- `attach_default_client_scopes`

## Role Management

- `list_realm_roles`
- `create_realm_role`
- `get_realm_role`
- `update_realm_role`
- `delete_realm_role`
- `list_client_roles`
- `create_client_role`
- `update_client_role`
- `assign_composite_roles`
- `remove_composite_roles`
- `users_with_role`
- `groups_with_role`

## Group Management

- `list_groups`
- `get_group`
- `create_group`
- `update_group`
- `delete_group`
- `create_child_group`
- `list_group_members`
- `add_group_member`
- `remove_group_member`
- `group_hierarchy`
- `update_group_attributes`

## Organization Management

- `list_organizations`
- `get_organization`
- `create_organization`
- `update_organization`
- `delete_organization`
- `list_organization_members`
- `add_organization_member`
- `remove_organization_member`

## Identity Provider Management

- `list_identity_providers`
- `get_identity_provider`
- `create_identity_provider`
- `update_identity_provider`
- `delete_identity_provider`
- `list_identity_provider_mappers`
- `create_identity_provider_mapper`
- `update_identity_provider_mapper`
- `delete_identity_provider_mapper`

## Client Scope Management

- `list_client_scopes`
- `get_client_scope`
- `create_client_scope`
- `update_client_scope`
- `delete_client_scope`
- `list_client_scope_mappers`
- `create_client_scope_mapper`
- `attach_default_scopes`
- `attach_optional_scopes`

## Session Management

- `list_active_sessions`
- `list_offline_sessions`
- `session_statistics`
- `terminate_session`
- `logout_all_sessions`

## Event Management

- `query_events`
- `query_admin_events`
- `update_event_config`
- `clear_events`
- `clear_admin_events`
