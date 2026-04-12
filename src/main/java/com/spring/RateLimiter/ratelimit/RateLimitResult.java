package com.spring.RateLimiter.ratelimit;

public record RateLimitResult(
        boolean allowed,
        int remaining,
        long resetAfterMs
) {}
