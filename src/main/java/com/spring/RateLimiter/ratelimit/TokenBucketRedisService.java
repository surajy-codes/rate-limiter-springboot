package com.spring.RateLimiter.ratelimit;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class TokenBucketRedisService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private DefaultRedisScript<Long> redisScript;

    @PostConstruct
    public void loadScript() {
        redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(
                new ResourceScriptSource(new ClassPathResource("token_bucket.lua"))
        );
        redisScript.setResultType(Long.class);
    }

    private final int CAPACITY = 5;
    private final int REFILL_RATE = 1;
    private final long REFILL_INTERVAL_SEC = 10; // 10 seconds — NOT milliseconds anymore

    public RateLimitResult check(String clientId) {
        String key = "token_bucket:" + clientId;
        long nowSec = System.currentTimeMillis() / 1000; // convert to seconds

        Long allowed = redisTemplate.execute(
                redisScript,
                Collections.singletonList(key),
                String.valueOf(CAPACITY),
                String.valueOf(REFILL_RATE),
                String.valueOf(REFILL_INTERVAL_SEC),
                String.valueOf(nowSec)
        );


        boolean isAllowed = allowed != null && allowed == 1L;
        return new RateLimitResult(isAllowed, 0, REFILL_INTERVAL_SEC * 1000);
    }
}