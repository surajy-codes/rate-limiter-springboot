package com.spring.RateLimiter.ratelimit;

import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RateLimiterService {

    private final ConcurrentHashMap<String, AtomicInteger> counters
            = new ConcurrentHashMap<>();

    private final int LIMIT = 5;           // max requests
    private final long WINDOW_MS = 60_000; //1 min

    public boolean isAllowed(String clientId) {
        String key = buildKey(clientId);

        counters.putIfAbsent(key, new AtomicInteger(0));
        AtomicInteger counter = counters.get(key);

        int currentCount = counter.incrementAndGet();
        return currentCount <= LIMIT;
    }

    private String buildKey(String clientId) {
        //what we are doing is just diving current time by 1 min,
        // which truncates everything below a minute as it is integer
        //now the user will have the same key for 1 minute, so we would be able to track the client
        long windowStart = System.currentTimeMillis() / WINDOW_MS;
        return clientId + ":" + windowStart;
    }
}
