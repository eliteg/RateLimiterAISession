package com.interviewscanner.ratelimiteraisession.ratelimiter;

public record RateLimitResult(boolean allowed, long remaining, long retryAfterMs) {
}
