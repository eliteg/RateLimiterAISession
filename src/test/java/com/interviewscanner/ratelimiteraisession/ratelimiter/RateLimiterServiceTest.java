package com.interviewscanner.ratelimiteraisession.ratelimiter;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

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

    @Test
    void neverAllowsMoreThanCapacityWhenBucketIsCreatedConcurrently() throws InterruptedException {
        int capacity = 100;
        RateLimiterService service = new RateLimiterService(
                Map.of("/search", new TokenBucketConfig(capacity, 0.0)),
                DEFAULT_CONFIG);
        int threadCount = 50;
        int attemptsPerThread = 10;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger allowedCount = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < attemptsPerThread; j++) {
                        if (service.checkLimit("client1", "/search", 0L).allowed()) {
                            allowedCount.incrementAndGet();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        assertThat(allowedCount.get()).isEqualTo(capacity);
    }
}
