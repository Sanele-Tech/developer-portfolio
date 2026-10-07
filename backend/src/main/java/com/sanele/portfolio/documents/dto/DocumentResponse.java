package com.sanele.portfolio.documents.dto;

import java.time.Instant;

public record DocumentResponse(Long id, String name, String documentType, String storageUrl,
        String mimeType, Long sizeBytes, Instant createdAt) {
}
