package com.sanele.portfolio.content.dto;

import java.time.Instant;

public record PageSummaryResponse(String slug, String title, String excerpt, Instant updatedAt) {
}
