package com.sanele.portfolio.analytics.controller;

import com.sanele.portfolio.analytics.dto.AnalyticsEventRequest;
import com.sanele.portfolio.analytics.service.AnalyticsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics/events")
public class AnalyticsEventController {
    private final AnalyticsService analyticsService;

    public AnalyticsEventController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void record(@Valid @RequestBody AnalyticsEventRequest request) {
        analyticsService.record(request);
    }
}
