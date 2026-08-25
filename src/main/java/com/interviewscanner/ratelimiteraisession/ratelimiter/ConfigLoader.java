package com.interviewscanner.ratelimiteraisession.ratelimiter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigLoader {

    private static final Map<String, Class<? extends AlgoConfig>> ALGORITHM_TYPES = Map.of(
            "TokenBucket", TokenBucketConfig.class,
            "SlidingWindow", SlidingWindowConfig.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, AlgoConfig> parse(String json) {
        try {
            List<ConfigEntry> entries = objectMapper.readValue(json, new TypeReference<List<ConfigEntry>>() {
            });

            Map<String, AlgoConfig> configs = new HashMap<>();
            for (ConfigEntry entry : entries) {
                Class<? extends AlgoConfig> algoConfigType = ALGORITHM_TYPES.get(entry.algorithm());
                if (algoConfigType == null) {
                    throw new IllegalArgumentException("Unsupported algorithm: " + entry.algorithm());
                }
                configs.put(entry.endpoint(), objectMapper.treeToValue(entry.algoConfig(), algoConfigType));
            }
            return configs;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to parse rate limiter config", e);
        }
    }

    public Map<String, AlgoConfig> loadFromClasspath(String resourcePath) {
        try (InputStream in = ConfigLoader.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IllegalArgumentException("Config resource not found: " + resourcePath);
            }
            String json = new String(in.readAllBytes());
            return parse(json);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read rate limiter config: " + resourcePath, e);
        }
    }
}
