package com.interviewscanner.ratelimiteraisession.ratelimiter;

// The config interface — each config knows how to build its own limiter
public interface AlgoConfig {
    Limiter createLimiter();

}
