package com.sanele.portfolio.analytics.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import java.util.Locale;

public enum AnalyticsEventType {
    PAGE_VIEW,
    PROJECT_VIEW,
    PROJECT_GITHUB_CLICK,
    PROJECT_DEMO_CLICK,
    RESUME_VIEW,
    RESUME_DOWNLOAD,
    CONTACT_FORM_OPEN,
    CONTACT_FORM_SUBMIT,
    SOCIAL_LINK_CLICK;

    @JsonValue
    public String value() {
        return name().toLowerCase(Locale.ROOT);
    }

    @JsonCreator
    public static AnalyticsEventType fromValue(String value) {
        return Arrays.stream(values())
                .filter(type -> type.value().equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported analytics event."));
    }
}
