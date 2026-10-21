package io.github.sakc.keycloak.mcp.domain.client;

import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.http.HttpHeaders;

public record KeycloakResponse(int status, HttpHeaders headers, JsonNode body) {

    public String header(String name) {
        return headers.getFirst(name);
    }
}
