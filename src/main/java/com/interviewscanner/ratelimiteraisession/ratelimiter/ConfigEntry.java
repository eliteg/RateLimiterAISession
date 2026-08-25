package com.interviewscanner.ratelimiteraisession.ratelimiter;

import com.fasterxml.jackson.databind.JsonNode;

record ConfigEntry(String endpoint, String algorithm, JsonNode algoConfig) {
}
