package com.sanele.portfolio.analytics.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record AnalyticsEventRequest(
        @NotNull UUID sessionId,
        @NotNull AnalyticsEventType eventName,
        @NotBlank @Size(max = 500) @Pattern(regexp = "^/(?!/).*") String path) {
}
