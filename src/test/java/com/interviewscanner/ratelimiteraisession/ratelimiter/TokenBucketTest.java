package com.interviewscanner.ratelimiteraisession.ratelimiter;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class TokenBucketTest {

    @Test
    void allowsRequestWhenTokensAvailable() {
        TokenBucket bucket = new TokenBucket(10, 1.0);

        RateLimitResult result = bucket.tryAcquire(0L);

        assertThat(result.allowed()).isTrue();
        assertThat(result.remaining()).isEqualTo(9);
        assertThat(result.retryAfterMs()).isEqualTo(0);
    }

    @Test
    void deniesRequestWhenNoTokensLeft() {
        TokenBucket bucket = new TokenBucket(1, 1.0);
        bucket.tryAcquire(0L);

        RateLimitResult result = bucket.tryAcquire(0L);

        assertThat(result.allowed()).isFalse();
        assertThat(result.remaining()).isEqualTo(0);
        assertThat(result.retryAfterMs()).isEqualTo(1000);
    }

    @Test
    void refillsTokensBasedOnElapsedTime() {
        TokenBucket bucket = new TokenBucket(10, 1.0);
        long time = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            bucket.tryAcquire(time);
        }

        RateLimitResult result = bucket.tryAcquire(System.currentTimeMillis()+ 5000);

        assertThat(result.allowed()).isTrue();
        assertThat(result.remaining()).isEqualTo(4);
    }

    @Test
    void doesNotRefillBeyondCapacity() {
        TokenBucket bucket = new TokenBucket(5, 10.0);

        RateLimitResult result = bucket.tryAcquire(10_000L);

        assertThat(result.allowed()).isTrue();
        assertThat(result.remaining()).isEqualTo(4);
    }

    @Test
    void neverAllowsMoreThanCapacityUnderConcurrentAccess() throws InterruptedException {
        int capacity = 100;
        TokenBucket bucket = new TokenBucket(capacity, 0.0);
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
                        if (bucket.tryAcquire(0L).allowed()) {
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
