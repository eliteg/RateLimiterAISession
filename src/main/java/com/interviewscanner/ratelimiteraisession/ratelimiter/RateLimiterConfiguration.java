package com.interviewscanner.ratelimiteraisession.ratelimiter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class RateLimiterConfiguration {

    private static final String CONFIG_RESOURCE = "rate-limit-config.json";
    private static final TokenBucketConfig DEFAULT_CONFIG = new TokenBucketConfig(100, 10);

    @Bean
    public RateLimiterService rateLimiterService() {
        Map<String, TokenBucketConfig> configs = new ConfigLoader().loadFromClasspath(CONFIG_RESOURCE);
        return new RateLimiterService(configs, DEFAULT_CONFIG);
    }
}
