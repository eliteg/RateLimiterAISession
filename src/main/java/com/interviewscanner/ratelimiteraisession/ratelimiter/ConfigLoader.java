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

    private static final String SUPPORTED_ALGORITHM = "TokenBucket";

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, TokenBucketConfig> parse(String json) {
        try {
            List<ConfigEntry> entries = objectMapper.readValue(json, new TypeReference<List<ConfigEntry>>() {
            });

            Map<String, TokenBucketConfig> configs = new HashMap<>();
            for (ConfigEntry entry : entries) {
                if (!SUPPORTED_ALGORITHM.equals(entry.algorithm())) {
                    throw new IllegalArgumentException("Unsupported algorithm: " + entry.algorithm());
                }
                configs.put(entry.endpoint(), entry.algoConfig());
            }
            return configs;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to parse rate limiter config", e);
        }
    }

    public Map<String, TokenBucketConfig> loadFromClasspath(String resourcePath) {
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
