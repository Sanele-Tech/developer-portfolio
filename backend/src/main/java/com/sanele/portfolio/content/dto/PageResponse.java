package com.sanele.portfolio.content.dto;

import com.sanele.portfolio.content.entity.Page.Status;
import java.time.Instant;

public record PageResponse(Long id, String slug, String title, String excerpt, String content,
        Status status, Instant createdAt, Instant updatedAt) {
}
