package com.sanele.portfolio.audit.dto;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;

public record AuditLogResponse(Long id, String actor, String action, String entityType,
        Long entityId, JsonNode details, Instant createdAt) {
}
