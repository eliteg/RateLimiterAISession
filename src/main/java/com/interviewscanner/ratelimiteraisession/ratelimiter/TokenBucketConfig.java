package com.interviewscanner.ratelimiteraisession.ratelimiter;

public record TokenBucketConfig(long capacity, double refillRatePerSecond) {
}
