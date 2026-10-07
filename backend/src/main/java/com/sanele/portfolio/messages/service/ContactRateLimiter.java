package com.sanele.portfolio.messages.service;

import com.sanele.portfolio.messages.dto.ContactRateLimitExceededException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ContactRateLimiter {
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();
    private final int maximumSubmissions;
    private final long windowMillis;

    public ContactRateLimiter(
            @Value("${portfolio.contact.rate-limit.maximum:5}") int maximumSubmissions,
            @Value("${portfolio.contact.rate-limit.window:15m}") Duration window) {
        if (maximumSubmissions < 1 || window.isZero() || window.isNegative()) {
            throw new IllegalArgumentException("Contact rate limit must use a positive count and duration.");
        }
        this.maximumSubmissions = maximumSubmissions;
        this.windowMillis = window.toMillis();
    }

    public void check(String clientAddress) {
        long now = System.currentTimeMillis();
        if (windows.size() > 10_000) {
            windows.entrySet().removeIf(entry -> now - entry.getValue().startedAt() >= windowMillis);
        }
        boolean[] allowed = { true };
        windows.compute(clientAddress, (key, previous) -> {
            if (previous == null || now - previous.startedAt() >= windowMillis) {
                return new Window(now, 1);
            }
            if (previous.count() >= maximumSubmissions) {
                allowed[0] = false;
                return previous;
            }
            return new Window(previous.startedAt(), previous.count() + 1);
        });
        if (!allowed[0]) throw new ContactRateLimitExceededException();
    }

    private record Window(long startedAt, int count) { }
}
