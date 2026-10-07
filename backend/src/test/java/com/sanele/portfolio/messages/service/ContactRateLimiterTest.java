package com.sanele.portfolio.messages.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.sanele.portfolio.messages.dto.ContactRateLimitExceededException;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class ContactRateLimiterTest {
    @Test
    void limitsEachClientWithinTheConfiguredWindow() {
        ContactRateLimiter limiter = new ContactRateLimiter(2, Duration.ofMinutes(15));

        assertDoesNotThrow(() -> limiter.check("127.0.0.1"));
        assertDoesNotThrow(() -> limiter.check("127.0.0.1"));
        assertThrows(ContactRateLimitExceededException.class, () -> limiter.check("127.0.0.1"));
        assertDoesNotThrow(() -> limiter.check("127.0.0.2"));
    }
}
