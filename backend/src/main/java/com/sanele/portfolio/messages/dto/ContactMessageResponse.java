package com.sanele.portfolio.messages.dto;

import com.sanele.portfolio.messages.entity.ContactMessage.Status;
import java.time.Instant;

public record ContactMessageResponse(Long id, String name, String email, String subject,
        String company, String message, Status status, Instant createdAt) {
    public ContactMessageResponse(Long id, String name, String email, String subject,
            String message, Status status, Instant createdAt) {
        this(id, name, email, subject, null, message, status, createdAt);
    }
}
