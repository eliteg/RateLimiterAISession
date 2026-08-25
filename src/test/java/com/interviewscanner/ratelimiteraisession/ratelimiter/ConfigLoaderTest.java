package com.interviewscanner.ratelimiteraisession.ratelimiter;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConfigLoaderTest {

    private final ConfigLoader configLoader = new ConfigLoader();

    @Test
    void parsesSingleEndpointConfig() {
        String json = """
                [
                  { "endpoint": "/search", "algorithm": "TokenBucket",
                    "algoConfig": { "capacity": 1000, "refillRatePerSecond": 10 } }
                ]
                """;

        Map<String, AlgoConfig> configs = configLoader.parse(json);

        assertThat(configs).containsOnlyKeys("/search");
        assertThat(configs.get("/search"))
                .usingRecursiveComparison()
                .isEqualTo(new TokenBucketConfig(1000, 10));
    }

    @Test
    void parsesMultipleEndpointConfigs() {
        String json = """
                [
                  { "endpoint": "/search", "algorithm": "TokenBucket",
                    "algoConfig": { "capacity": 1000, "refillRatePerSecond": 10 } },
                  { "endpoint": "/checkout", "algorithm": "TokenBucket",
                    "algoConfig": { "capacity": 50, "refillRatePerSecond": 5 } }
                ]
                """;

        Map<String, AlgoConfig> configs = configLoader.parse(json);

        assertThat(configs).containsOnlyKeys("/search", "/checkout");
        assertThat(configs.get("/search"))
                .usingRecursiveComparison()
                .isEqualTo(new TokenBucketConfig(1000, 10));
        assertThat(configs.get("/checkout"))
                .usingRecursiveComparison()
                .isEqualTo(new TokenBucketConfig(50, 5));
    }

    @Test
    void parsesSlidingWindowConfig() {
        String json = """
                [
                  { "endpoint": "/login", "algorithm": "SlidingWindow",
                    "algoConfig": { "windowMs": 60000, "maxRequests": 5 } }
                ]
                """;

        Map<String, AlgoConfig> configs = configLoader.parse(json);

        assertThat(configs).containsOnlyKeys("/login");
        assertThat(configs.get("/login"))
                .usingRecursiveComparison()
                .isEqualTo(new SlidingWindowConfig(60000, 5));
    }

    @Test
    void throwsOnUnsupportedAlgorithm() {
        String json = """
                [
                  { "endpoint": "/search", "algorithm": "LeakyBucket",
                    "algoConfig": { "capacity": 1000, "refillRatePerSecond": 10 } }
                ]
                """;

        assertThatThrownBy(() -> configLoader.parse(json))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("LeakyBucket");
    }

    @Test
    void loadsConfigFromClasspathResource() {
        Map<String, AlgoConfig> configs = configLoader.loadFromClasspath("rate-limit-config-test.json");

        assertThat(configs).containsOnlyKeys("/search");
        assertThat(configs.get("/search"))
                .usingRecursiveComparison()
                .isEqualTo(new TokenBucketConfig(1000, 10));
    }
}
