package io.github.sakc.keycloak.mcp.config;

import java.util.UUID;

import io.github.sakc.keycloak.mcp.domain.security.SlidingWindowRateLimiter;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.server.WebFilter;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    SlidingWindowRateLimiter slidingWindowRateLimiter(ToolSecurityProperties properties) {
        return new SlidingWindowRateLimiter(properties);
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    WebFilter correlationIdFilter() {
        return (exchange, chain) -> {
            String correlationId = exchange.getRequest().getHeaders().getFirst("X-Correlation-Id");
            String resolved = correlationId == null || correlationId.isBlank() ? UUID.randomUUID().toString() : correlationId;
            exchange.getResponse().getHeaders().set("X-Correlation-Id", resolved);
            MDC.put("correlationId", resolved);
            return chain.filter(exchange)
                .doFinally(signalType -> MDC.remove("correlationId"));
        };
    }

    @Bean
    SecurityWebFilterChain springSecurityFilterChain(
        ServerHttpSecurity http,
        ToolSecurityProperties properties,
        StreamableHttpEnterpriseProperties streamableHttpProperties
    ) {
        String mcpEndpoint = streamableHttpProperties.endpoint();
        http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
            .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
            .authorizeExchange(spec -> spec
                .pathMatchers("/actuator/health/**", "/actuator/info", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .pathMatchers(mcpEndpoint, mcpEndpoint + "/**").access((authentication, context) -> properties.enforceHttpJwt()
                    ? authentication.map(auth -> new org.springframework.security.authorization.AuthorizationDecision(auth.isAuthenticated()))
                    : reactor.core.publisher.Mono.just(new org.springframework.security.authorization.AuthorizationDecision(true)))
                .anyExchange().permitAll());

        if (properties.enforceHttpJwt()) {
            http.oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults()));
        }

        return http.build();
    }
}
