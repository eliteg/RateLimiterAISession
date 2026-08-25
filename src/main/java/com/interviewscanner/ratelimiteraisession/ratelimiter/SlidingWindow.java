package com.interviewscanner.ratelimiteraisession.ratelimiter;

import java.util.ArrayDeque;
import java.util.Deque;

class SlidingWindow implements Limiter {
    private final long windowMs;      // e.g. 1000 = a 1-second window
    private final long maxRequests;   // e.g. 5 = at most 5 requests per window
    private final Deque<Long> timestamps = new ArrayDeque<>();

    public SlidingWindow(long windowMs, long maxRequests) {
        this.windowMs = windowMs;
        this.maxRequests = maxRequests;
    }

    @Override
    public synchronized RateLimitResult tryAcquire(long nowMs) {
        long windowStart = nowMs - windowMs;

        // drop timestamps that fell out of the window
        while (!timestamps.isEmpty() && timestamps.peekFirst() <= windowStart) {
            timestamps.pollFirst();
        }

        if (timestamps.size() < maxRequests) {
            timestamps.addLast(nowMs);
            long remaining = maxRequests - timestamps.size();
            return new RateLimitResult(true, remaining, 0L);
        }

        // window full — retry when the oldest request ages out
        long oldest = timestamps.peekFirst();
        long retryAfterMs = (oldest + windowMs) - nowMs;
        return new RateLimitResult(false, 0L, retryAfterMs);
    }
}