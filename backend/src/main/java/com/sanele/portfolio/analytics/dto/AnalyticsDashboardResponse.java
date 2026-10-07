package com.sanele.portfolio.analytics.dto;

import java.time.LocalDate;
import java.util.List;

public record AnalyticsDashboardResponse(
        LocalDate from,
        LocalDate to,
        long visits,
        long pageViews,
        long resumeViews,
        long resumeDownloads,
        long contactFormOpens,
        long contactFormSubmissions,
        double contactConversionRate,
        List<DailyVisit> visitsOverTime,
        List<RankedPage> topPages,
        List<RankedProject> topProjects,
        List<EventCount> eventCounts) {

    public record DailyVisit(LocalDate date, long visits) { }
    public record RankedPage(String path, long views) { }
    public record RankedProject(String path, long views) { }
    public record EventCount(String eventName, long count) { }
}
