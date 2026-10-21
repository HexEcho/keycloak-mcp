package io.github.sakc.keycloak.mcp.domain.auth;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.sakc.keycloak.mcp.config.KeycloakAdminProperties;
import io.github.sakc.keycloak.mcp.domain.error.AuthenticationException;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class AdminAccessTokenProvider {

    private final WebClient keycloakWebClient;
    private final KeycloakAdminProperties properties;
    private final ObjectMapper objectMapper;
    private final AtomicReference<CachedToken> cachedToken = new AtomicReference<>();

    public AdminAccessTokenProvider(WebClient keycloakWebClient, KeycloakAdminProperties properties, ObjectMapper objectMapper) {
        this.keycloakWebClient = keycloakWebClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public String getAccessToken() {
        CachedToken current = cachedToken.get();
        if (current != null && current.expiresAt().isAfter(Instant.now().plusSeconds(30))) {
            return current.token();
        }

        JsonNode tokenResponse = keycloakWebClient.post()
            .uri(properties.tokenPath().formatted(properties.adminRealm()))
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(BodyInserters.fromValue(formBody()))
            .retrieve()
            .bodyToMono(String.class)
            .map(this::parseTokenResponse)
            .block(properties.http().responseTimeout());

        if (tokenResponse == null || tokenResponse.path("access_token").asText().isBlank()) {
            throw new AuthenticationException("Unable to retrieve Keycloak admin access token", 401, null, null, null);
        }

        Instant expiresAt = Instant.now().plusSeconds(Math.max(60, tokenResponse.path("expires_in").asLong(300) - 30));
        CachedToken refreshed = new CachedToken(tokenResponse.path("access_token").asText(), expiresAt);
        cachedToken.set(refreshed);
        return refreshed.token();
    }

    private String formBody() {
        Map<String, String> values = new LinkedHashMap<>();
        KeycloakAdminProperties.Auth auth = properties.auth();
        KeycloakAdminProperties.AuthMode mode = auth.mode() == null ? KeycloakAdminProperties.AuthMode.CLIENT_CREDENTIALS : auth.mode();

        values.put("client_id", nullToEmpty(auth.clientId()));
        if (auth.scope() != null && !auth.scope().isBlank()) {
            values.put("scope", auth.scope());
        }
        switch (mode) {
            case PASSWORD -> {
                values.put("grant_type", "password");
                values.put("username", nullToEmpty(auth.username()));
                values.put("password", nullToEmpty(auth.password()));
            }
            case SERVICE_ACCOUNT, CLIENT_CREDENTIALS -> values.put("grant_type", "client_credentials");
            case TOKEN_EXCHANGE -> values.put("grant_type", "urn:ietf:params:oauth:grant-type:token-exchange");
            case MTLS -> values.put("grant_type", "client_credentials");
        }
        if (auth.clientSecret() != null && !auth.clientSecret().isBlank()) {
            values.put("client_secret", auth.clientSecret());
        }
        return values.entrySet().stream()
            .map(entry -> urlEncode(entry.getKey()) + "=" + urlEncode(entry.getValue()))
            .reduce((left, right) -> left + "&" + right)
            .orElse("");
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private JsonNode parseTokenResponse(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return objectMapper.createObjectNode();
        }
        try {
            return objectMapper.readTree(responseBody);
        }
        catch (Exception exception) {
            throw new AuthenticationException(
                "Unable to parse Keycloak token response",
                500,
                responseBody,
                null,
                null
            );
        }
    }

    private record CachedToken(String token, Instant expiresAt) {
    }
}
