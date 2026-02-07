package com.xxxx.votting_spring.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitingService {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    // Login/Register: 5 requests per minute
    public Bucket resolveBucket(String apiKey) {
        return cache.computeIfAbsent(apiKey, this::newBucket);
    }

    // General API: 100 requests per minute
    public Bucket resolveGeneralBucket(String apiKey) {
        return cache.computeIfAbsent("GENERAL_" + apiKey, key -> {
            // 100 tokens regenerated per minute
            Bandwidth limit = Bandwidth.builder()
                    .capacity(100)
                    .refillGreedy(100, Duration.ofMinutes(1))
                    .build();
            return Bucket.builder().addLimit(limit).build();
        });
    }

    private Bucket newBucket(String apiKey) {
        // 5 tokens regenerated per minute (Strict for Auth)
        Bandwidth limit = Bandwidth.builder()
                .capacity(5)
                .refillGreedy(5, Duration.ofMinutes(1))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }
}
