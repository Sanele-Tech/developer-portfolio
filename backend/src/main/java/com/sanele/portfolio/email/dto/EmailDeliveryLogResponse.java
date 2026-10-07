package com.sanele.portfolio.email.dto;

import java.time.Instant;

public record EmailDeliveryLogResponse(Long id, Long contactMessageId, String templateName,
        String status, String failureReason, Instant createdAt) {
}
