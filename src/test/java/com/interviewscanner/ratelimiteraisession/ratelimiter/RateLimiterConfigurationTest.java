package com.interviewscanner.ratelimiteraisession.ratelimiter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RateLimiterConfigurationTest {

    @Autowired
    private RateLimiterService rateLimiterService;

    @Test
    void wiresConfiguredEndpointFromStartupResource() {
        for (int i = 0; i < 1000; i++) {
            RateLimitResult result = rateLimiterService.checkLimit("client1", "/search", 0L);
            assertThat(result.allowed()).isTrue();
        }

        RateLimitResult overLimit = rateLimiterService.checkLimit("client1", "/search", 0L);

        assertThat(overLimit.allowed()).isFalse();
    }

    @Test
    void fallsBackToDefaultConfigForUnconfiguredEndpoint() {
        RateLimitResult result = rateLimiterService.checkLimit("client1", "/unconfigured", 0L);

        assertThat(result.allowed()).isTrue();
    }
}
