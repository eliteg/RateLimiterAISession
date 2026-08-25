package com.interviewscanner.ratelimiteraisession.ratelimiter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

// ---------- TokenBucket config + implementation ----------
class TokenBucketConfig implements AlgoConfig {
    private final long capacity;
    private final double refillRatePerSecond;

    @JsonCreator
    public TokenBucketConfig(
            @JsonProperty("capacity") long capacity,
            @JsonProperty("refillRatePerSecond") double refillRatePerSecond) {
        this.capacity = capacity;
        this.refillRatePerSecond = refillRatePerSecond;
    }

    @Override
    public Limiter createLimiter() {
        return new TokenBucket(capacity, refillRatePerSecond);
    }
}