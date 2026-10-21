# Roadmap

## Near Term

- Expand tool coverage for realm-specific authentication execution flows.
- Add richer DTOs per Keycloak endpoint instead of map-based payloads for every mutation.
- Introduce policy-based allow-listing per realm and per tool family.

## Mid Term

- Add transport context extraction for richer audit identities.
- Add caching for read-heavy realm and client lookups.
- Add contract-test snapshots against multiple Keycloak 26.x releases.

## Long Term

- Track Keycloak Admin API v2 changes through dedicated compatibility tests.
- Introduce plugin jars for optional Keycloak domains and organization-specific tools.
- Add token exchange and mutual TLS authentication providers.
