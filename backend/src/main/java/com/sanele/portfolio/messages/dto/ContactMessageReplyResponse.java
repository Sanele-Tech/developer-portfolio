package com.sanele.portfolio.messages.dto;

import java.time.Instant;

public record ContactMessageReplyResponse(Long id, String author, String message,
        Instant createdAt, boolean emailDelivered) {
}
