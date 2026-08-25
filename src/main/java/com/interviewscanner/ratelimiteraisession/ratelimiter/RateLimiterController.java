package com.interviewscanner.ratelimiteraisession.ratelimiter;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RateLimiterController {

    private final RateLimiterService rateLimiterService;

    public RateLimiterController(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @PostMapping("/api/rate-limit/check")
    public ResponseEntity<RateLimitResult> check(@RequestBody RateLimitCheckRequest request) {
        RateLimitResult result = rateLimiterService.checkLimit(request.clientId(), request.endpoint(), System.currentTimeMillis());

        if (!result.allowed()) {
            long retryAfterSeconds = (long) Math.ceil(result.retryAfterMs() / 1000.0);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .header("Retry-After", String.valueOf(retryAfterSeconds))
                    .body(result);
        }

        return ResponseEntity.ok(result);
    }
}
