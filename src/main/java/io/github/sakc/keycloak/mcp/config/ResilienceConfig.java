package io.github.sakc.keycloak.mcp.config;

import java.time.Duration;

import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResilienceConfig {

    @Bean
    CircuitBreakerRegistry circuitBreakerRegistry(KeycloakAdminProperties properties) {
        KeycloakAdminProperties.Resilience resilience = properties.resilience();
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
            .failureRateThreshold(resilience.failureRateThreshold())
            .slowCallRateThreshold(resilience.slowCallRateThreshold())
            .slowCallDurationThreshold(resilience.timeout())
            .minimumNumberOfCalls(5)
            .slidingWindowSize(20)
            .build();
        return CircuitBreakerRegistry.of(config);
    }

    @Bean
    RetryRegistry retryRegistry(KeycloakAdminProperties properties) {
        KeycloakAdminProperties.Resilience resilience = properties.resilience();
        RetryConfig config = RetryConfig.custom()
            .maxAttempts(resilience.maxAttempts())
            .intervalFunction(IntervalFunction.ofExponentialBackoff(resilience.retryBackoff().toMillis(), 2.0d))
            .retryExceptions(java.io.IOException.class, java.util.concurrent.TimeoutException.class)
            .build();
        return RetryRegistry.of(config);
    }

    @Bean
    BulkheadRegistry bulkheadRegistry(KeycloakAdminProperties properties) {
        BulkheadConfig config = BulkheadConfig.custom()
            .maxConcurrentCalls(properties.resilience().maxConcurrentCalls())
            .maxWaitDuration(Duration.ofMillis(100))
            .build();
        return BulkheadRegistry.of(config);
    }

    @Bean
    TimeLimiterRegistry timeLimiterRegistry(KeycloakAdminProperties properties) {
        TimeLimiterConfig config = TimeLimiterConfig.custom()
            .timeoutDuration(properties.resilience().timeout())
            .cancelRunningFuture(true)
            .build();
        return TimeLimiterRegistry.of(config);
    }
}
