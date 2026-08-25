package com.interviewscanner.ratelimiteraisession.ratelimiter;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimiterService {
    private final Map<String, AlgoConfig> configs;          // per endpoint, shared, immutable
    private final AlgoConfig defaultConfig;                 // non-null, enforced
    private final Map<ClientEndpointKey, Limiter> limiters  // per (client, endpoint), stateful
            = new ConcurrentHashMap<>();

    public RateLimiterService(Map<String, AlgoConfig> configs, AlgoConfig defaultConfig) {
        this.configs = Objects.requireNonNull(configs, "configs");
        this.defaultConfig = Objects.requireNonNull(defaultConfig, "defaultConfig");
    }

    public RateLimitResult checkLimit(String clientId, String endpoint, long nowMs) {
        ClientEndpointKey key = new ClientEndpointKey(clientId, endpoint);
        Limiter limiter = limiters.computeIfAbsent(key, k -> {
            AlgoConfig cfg = configs.getOrDefault(endpoint, defaultConfig);
            return cfg.createLimiter();
        });
        return limiter.tryAcquire(nowMs);
    }

}