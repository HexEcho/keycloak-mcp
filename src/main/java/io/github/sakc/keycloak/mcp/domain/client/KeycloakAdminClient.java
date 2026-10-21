package io.github.sakc.keycloak.mcp.domain.client;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.sakc.keycloak.mcp.config.KeycloakAdminProperties;
import io.github.sakc.keycloak.mcp.domain.auth.AdminAccessTokenProvider;
import io.github.sakc.keycloak.mcp.domain.error.KeycloakAdminException;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.tracing.Tracer;
import org.slf4j.MDC;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;

@Component
public class KeycloakAdminClient {

    private static final String CLIENT_NAME = "keycloakAdmin";

    private final WebClient webClient;
    private final AdminAccessTokenProvider tokenProvider;
    private final KeycloakAdminProperties properties;
    private final KeycloakExceptionMapper exceptionMapper;
    private final ObjectMapper objectMapper;
    private final CircuitBreaker circuitBreaker;
    private final Retry retry;
    private final Bulkhead bulkhead;
    private final TimeLimiter timeLimiter;
    private final MeterRegistry meterRegistry;
    private final Tracer tracer;
    private final ExecutorService requestExecutor = Executors.newVirtualThreadPerTaskExecutor();

    @Autowired
    public KeycloakAdminClient(
        WebClient keycloakWebClient,
        AdminAccessTokenProvider tokenProvider,
        KeycloakAdminProperties properties,
        KeycloakExceptionMapper exceptionMapper,
        ObjectMapper objectMapper,
        CircuitBreakerRegistry circuitBreakerRegistry,
        RetryRegistry retryRegistry,
        BulkheadRegistry bulkheadRegistry,
        TimeLimiterRegistry timeLimiterRegistry,
        MeterRegistry meterRegistry,
        ObjectProvider<Tracer> tracerProvider
    ) {
        this.webClient = keycloakWebClient;
        this.tokenProvider = tokenProvider;
        this.properties = properties;
        this.exceptionMapper = exceptionMapper;
        this.objectMapper = objectMapper;
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker(CLIENT_NAME);
        this.retry = retryRegistry.retry(CLIENT_NAME);
        this.bulkhead = bulkheadRegistry.bulkhead(CLIENT_NAME);
        this.timeLimiter = timeLimiterRegistry.timeLimiter(CLIENT_NAME);
        this.meterRegistry = meterRegistry;
        this.tracer = tracerProvider.getIfAvailable();
    }

    public KeycloakResponse get(String path, MultiValueMap<String, String> queryParams) {
        return exchange(HttpMethod.GET, path, queryParams, null);
    }

    public KeycloakResponse post(String path, Object body) {
        return exchange(HttpMethod.POST, path, null, body);
    }

    public KeycloakResponse put(String path, Object body) {
        return exchange(HttpMethod.PUT, path, null, body);
    }

    public KeycloakResponse delete(String path) {
        return exchange(HttpMethod.DELETE, path, null, null);
    }

    public KeycloakResponse exchange(HttpMethod method, String path, MultiValueMap<String, String> queryParams, Object body) {
        Supplier<KeycloakResponse> supplier = () -> doExchange(method, path, queryParams, body);
        try {
            Supplier<KeycloakResponse> bulkheadSupplier = Bulkhead.decorateSupplier(bulkhead, supplier);
            Supplier<KeycloakResponse> circuitSupplier = CircuitBreaker.decorateSupplier(circuitBreaker, bulkheadSupplier);
            Supplier<KeycloakResponse> retrySupplier = Retry.decorateSupplier(retry, circuitSupplier);
            return TimeLimiter.decorateFutureSupplier(timeLimiter,
                    () -> CompletableFuture.supplyAsync(retrySupplier, requestExecutor))
                .call();
        }
        catch (Exception exception) {
            if (exception instanceof KeycloakAdminException keycloakAdminException) {
                throw keycloakAdminException;
            }
            throw new KeycloakAdminException("keycloak_client_error", exception.getMessage(), 500, null, null, MDC.get("correlationId"));
        }
    }

    private KeycloakResponse doExchange(HttpMethod method, String path, MultiValueMap<String, String> queryParams, Object body) {
        Timer.Sample sample = Timer.start(meterRegistry);
        var span = tracer == null ? null : tracer.nextSpan().name("keycloak.rest.call").tag("http.method", method.name()).tag("keycloak.path", path).start();
        try (Tracer.SpanInScope ignored = tracer == null ? null : tracer.withSpan(span)) {
            String uri = buildUri(path, queryParams);
            RequestBodySpec request = webClient.method(method)
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenProvider.getAccessToken())
                .header("X-Correlation-Id", correlationId());

            KeycloakResponse response = (body != null && allowsRequestBody(method) ? request.bodyValue(body) : request)
                .exchangeToMono(clientResponse -> toResponse(clientResponse, path))
                .block(properties.http().responseTimeout());
            meterRegistry.counter("keycloak.admin.request.count", "method", method.name(), "path", path, "status", Integer.toString(response.status())).increment();
            return response;
        }
        finally {
            sample.stop(Timer.builder("keycloak.admin.request.latency").tag("method", method.name()).tag("path", path).register(meterRegistry));
            if (span != null) {
                span.end();
            }
        }
    }

    private reactor.core.publisher.Mono<KeycloakResponse> toResponse(ClientResponse response, String path) {
        return response.bodyToMono(String.class)
            .defaultIfEmpty("")
            .map(body -> {
                JsonNode jsonBody = parseBody(body);
                if (response.statusCode().isError()) {
                    throw exceptionMapper.map(response.statusCode(), jsonBody, path, response.headers().header("X-Request-Id").stream().findFirst().orElse(null), correlationId());
                }
                return new KeycloakResponse(response.statusCode().value(), response.headers().asHttpHeaders(), jsonBody);
            });
    }

    private JsonNode parseBody(String body) {
        if (body == null || body.isBlank()) {
            return JsonNodeFactory.instance.objectNode();
        }
        try {
            return objectMapper.readTree(body);
        }
        catch (Exception exception) {
            ObjectNode fallback = JsonNodeFactory.instance.objectNode();
            fallback.put("raw", body);
            return fallback;
        }
    }

    private String buildUri(String path, MultiValueMap<String, String> queryParams) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath(path);
        if (queryParams != null && !queryParams.isEmpty()) {
            builder.queryParams(queryParams);
        }
        return builder.build(true).toUriString();
    }

    private String correlationId() {
        return MDC.get("correlationId");
    }

    private boolean allowsRequestBody(HttpMethod method) {
        return HttpMethod.POST.equals(method) || HttpMethod.PUT.equals(method) || HttpMethod.PATCH.equals(method) || HttpMethod.DELETE.equals(method);
    }
}
