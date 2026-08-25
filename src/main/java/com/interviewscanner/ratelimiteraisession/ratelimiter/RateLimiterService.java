package com.interviewscanner.ratelimiteraisession.ratelimiter;

import java.util.HashMap;
import java.util.Map;

public class RateLimiterService {

    private final Map<String, TokenBucketConfig> configs;
    private final TokenBucketConfig defaultConfig;
    private final Map<ClientEndpointKey, TokenBucket> limiters = new HashMap<>();

    public RateLimiterService(Map<String, TokenBucketConfig> configs, TokenBucketConfig defaultConfig) {
        this.configs = configs;
        this.defaultConfig = defaultConfig;
    }

    public RateLimitResult checkLimit(String clientId, String endpoint, long nowMs) {
        ClientEndpointKey key = new ClientEndpointKey(clientId, endpoint);
        TokenBucket bucket = limiters.computeIfAbsent(key, k -> newBucketFor(endpoint));
        return bucket.tryAcquire(nowMs);
    }

    private TokenBucket newBucketFor(String endpoint) {
        TokenBucketConfig config = configs.getOrDefault(endpoint, defaultConfig);
        return new TokenBucket(config.capacity(), config.refillRatePerSecond());
    }
}
