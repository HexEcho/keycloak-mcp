package io.github.sakc.keycloak.mcp.config;

import java.time.Duration;

import io.micrometer.observation.ObservationRegistry;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties({KeycloakAdminProperties.class, ToolSecurityProperties.class})
public class WebClientConfig {

    @Bean
    ConnectionProvider keycloakConnectionProvider(KeycloakAdminProperties properties) {
        var http = properties.http();
        return ConnectionProvider.builder("keycloak-admin")
            .maxConnections(http.maxConnections())
            .pendingAcquireMaxCount(http.pendingAcquireMaxCount())
            .build();
    }

    @Bean
    WebClient keycloakWebClient(KeycloakAdminProperties properties, ConnectionProvider connectionProvider, ObservationRegistry observationRegistry) {
        Duration connectTimeout = properties.http().connectTimeout();
        HttpClient httpClient = HttpClient.create(connectionProvider)
            .compress(true)
            .responseTimeout(properties.http().responseTimeout())
            .option(io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS, Math.toIntExact(connectTimeout.toMillis()));

        return WebClient.builder()
            .baseUrl(properties.baseUrl())
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .observationRegistry(observationRegistry)
            .build();
    }
}
