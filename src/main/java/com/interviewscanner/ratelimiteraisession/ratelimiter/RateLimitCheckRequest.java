package com.interviewscanner.ratelimiteraisession.ratelimiter;

public record RateLimitCheckRequest(String clientId, String endpoint) {
}
