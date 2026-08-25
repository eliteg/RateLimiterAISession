package com.interviewscanner.ratelimiteraisession.ratelimiter;

record ConfigEntry(String endpoint, String algorithm, TokenBucketConfig algoConfig) {
}
