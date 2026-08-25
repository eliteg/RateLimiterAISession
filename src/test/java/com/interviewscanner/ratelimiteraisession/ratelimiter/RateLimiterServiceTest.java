package com.interviewscanner.ratelimiteraisession.ratelimiter;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimiterServiceTest {

    private static final TokenBucketConfig DEFAULT_CONFIG = new TokenBucketConfig(2, 1.0);

    @Test
    void usesEndpointConfigWhenPresent() {
        RateLimiterService service = new RateLimiterService(
                Map.of("/search", new TokenBucketConfig(1, 1.0)),
                DEFAULT_CONFIG);

        RateLimitResult first = service.checkLimit("client1", "/search", 0L);
        RateLimitResult second = service.checkLimit("client1", "/search", 0L);

        assertThat(first.allowed()).isTrue();
        assertThat(second.allowed()).isFalse();
    }

    @Test
    void fallsBackToDefaultConfigWhenEndpointUnconfigured() {
        RateLimiterService service = new RateLimiterService(Map.of(), DEFAULT_CONFIG);

        RateLimitResult result = service.checkLimit("client1", "/unconfigured", 0L);

        assertThat(result.allowed()).isTrue();
        assertThat(result.remaining()).isEqualTo(1);
    }

    @Test
    void tracksSeparateStatePerClientForSameEndpoint() {
        RateLimiterService service = new RateLimiterService(
                Map.of("/search", new TokenBucketConfig(1, 1.0)),
                DEFAULT_CONFIG);

        RateLimitResult clientA = service.checkLimit("clientA", "/search", 0L);
        RateLimitResult clientB = service.checkLimit("clientB", "/search", 0L);

        assertThat(clientA.allowed()).isTrue();
        assertThat(clientB.allowed()).isTrue();
    }

    @Test
    void tracksSeparateStatePerEndpointForSameClient() {
        RateLimiterService service = new RateLimiterService(
                Map.of(
                        "/search", new TokenBucketConfig(1, 1.0),
                        "/checkout", new TokenBucketConfig(1, 1.0)),
                DEFAULT_CONFIG);

        RateLimitResult search = service.checkLimit("client1", "/search", 0L);
        RateLimitResult checkout = service.checkLimit("client1", "/checkout", 0L);

        assertThat(search.allowed()).isTrue();
        assertThat(checkout.allowed()).isTrue();
    }
}
