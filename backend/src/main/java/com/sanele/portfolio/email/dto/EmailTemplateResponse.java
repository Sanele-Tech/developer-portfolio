package com.sanele.portfolio.email.dto;

import java.time.Instant;

public record EmailTemplateResponse(Long id, String name, String subject, String body,
        Instant createdAt, Instant updatedAt) {
}
