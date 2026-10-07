package com.sanele.portfolio.messages.dto;

import java.time.Instant;

public record ContactMessageReplyRecord(Long id, String author, String message, Instant createdAt) {
}
