package com.interviewscanner.ratelimiteraisession.ratelimiter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

// ---------- A second algorithm, to show extensibility ----------
class SlidingWindowConfig implements AlgoConfig {
    private final long windowMs;
    private final long maxRequests;

    @JsonCreator
    public SlidingWindowConfig(
            @JsonProperty("windowMs") long windowMs,
            @JsonProperty("maxRequests") long maxRequests) {
        this.windowMs = windowMs;
        this.maxRequests = maxRequests;
    }

    @Override
    public Limiter createLimiter() {
        return new SlidingWindow(windowMs, maxRequests);
    }
}

