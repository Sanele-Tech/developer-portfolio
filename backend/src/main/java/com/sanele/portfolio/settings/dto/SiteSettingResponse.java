package com.sanele.portfolio.settings.dto;

import java.time.Instant;

public record SiteSettingResponse(Long id, String settingKey, String settingValue, Instant updatedAt) {
}
