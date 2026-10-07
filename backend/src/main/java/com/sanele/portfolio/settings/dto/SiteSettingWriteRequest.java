package com.sanele.portfolio.settings.dto;

import jakarta.validation.constraints.Size;

public record SiteSettingWriteRequest(@Size(max = 5000) String settingValue) {
}
