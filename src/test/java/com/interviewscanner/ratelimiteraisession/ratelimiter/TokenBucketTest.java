package com.interviewscanner.ratelimiteraisession.ratelimiter;

import org.junit.jupiter.api.Test;

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
        for (int i = 0; i < 10; i++) {
            bucket.tryAcquire(0L);
        }

        RateLimitResult result = bucket.tryAcquire(5_000L);

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
}
