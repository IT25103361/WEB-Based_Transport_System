package com.webtrans.emergency.service;

import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EmergencyRateLimiter {

    // Maximum 6 emergency submissions per 10-minute sliding window per IP
    private static final int MAX_REQUESTS = 6;
    private static final long WINDOW_MILLIS = 10 * 60 * 1000L;

    private final Map<String, Deque<Long>> ipTimestamps = new ConcurrentHashMap<>();

    public synchronized boolean isAllowed(String clientIp) {
        if (clientIp == null || clientIp.isBlank() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = "anonymous";
        }

        long now = System.currentTimeMillis();
        Deque<Long> timestamps = ipTimestamps.computeIfAbsent(clientIp, k -> new ArrayDeque<>());

        // Evict expired timestamps
        while (!timestamps.isEmpty() && (now - timestamps.peekFirst()) > WINDOW_MILLIS) {
            timestamps.pollFirst();
        }

        if (timestamps.size() >= MAX_REQUESTS) {
            return false;
        }

        timestamps.addLast(now);
        return true;
    }

    public synchronized void resetForTesting() {
        ipTimestamps.clear();
    }
}
