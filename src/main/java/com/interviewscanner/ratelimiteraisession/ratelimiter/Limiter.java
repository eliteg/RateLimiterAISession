package com.interviewscanner.ratelimiteraisession.ratelimiter;

// The interface every algorithm implements

public interface Limiter {
    RateLimitResult tryAcquire(long nowMs);

}
