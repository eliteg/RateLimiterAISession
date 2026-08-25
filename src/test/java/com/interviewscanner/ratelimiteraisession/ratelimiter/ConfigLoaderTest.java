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

        Map<String, TokenBucketConfig> configs = configLoader.parse(json);

        assertThat(configs).containsExactly(
                Map.entry("/search", new TokenBucketConfig(1000, 10)));
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

        Map<String, TokenBucketConfig> configs = configLoader.parse(json);

        assertThat(configs).containsExactlyInAnyOrderEntriesOf(Map.of(
                "/search", new TokenBucketConfig(1000, 10),
                "/checkout", new TokenBucketConfig(50, 5)));
    }

    @Test
    void throwsOnUnsupportedAlgorithm() {
        String json = """
                [
                  { "endpoint": "/search", "algorithm": "SlidingWindow",
                    "algoConfig": { "capacity": 1000, "refillRatePerSecond": 10 } }
                ]
                """;

        assertThatThrownBy(() -> configLoader.parse(json))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SlidingWindow");
    }

    @Test
    void loadsConfigFromClasspathResource() {
        Map<String, TokenBucketConfig> configs = configLoader.loadFromClasspath("rate-limit-config-test.json");

        assertThat(configs).containsExactly(
                Map.entry("/search", new TokenBucketConfig(1000, 10)));
    }
}
