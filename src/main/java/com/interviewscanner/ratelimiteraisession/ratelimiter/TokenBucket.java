package com.interviewscanner.ratelimiteraisession.ratelimiter;

public class    TokenBucket {

    private final long capacity;
    private final double refillRatePerSecond;
    private double tokens;
    private long lastRefillMs;

    public TokenBucket(long capacity, double refillRatePerSecond) {
        this.capacity = capacity;
        this.refillRatePerSecond = refillRatePerSecond;
        this.tokens = capacity;
        this.lastRefillMs = 0L;
    }

    public RateLimitResult tryAcquire(long nowMs) {
        refill(nowMs);

        if (tokens >= 1) {
            tokens -= 1;
            return new RateLimitResult(true, (long) tokens, 0L);
        }

        long retryAfterMs = (long) Math.ceil((1 - tokens) / refillRatePerSecond * 1000);
        return new RateLimitResult(false, 0L, retryAfterMs);
    }

    private void refill(long nowMs) {
        long elapsedMs = Math.max(0, nowMs - lastRefillMs);
        double refilled = elapsedMs / 1000.0 * refillRatePerSecond;
        tokens = Math.min(capacity, tokens + refilled);
        lastRefillMs = nowMs;
    }
}
