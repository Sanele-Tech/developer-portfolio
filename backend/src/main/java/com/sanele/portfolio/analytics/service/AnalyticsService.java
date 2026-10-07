package com.sanele.portfolio.analytics.service;

import com.sanele.portfolio.analytics.dto.AnalyticsDashboardResponse;
import com.sanele.portfolio.analytics.dto.AnalyticsEventRequest;
import com.sanele.portfolio.analytics.dto.AnalyticsEventType;
import com.sanele.portfolio.analytics.entity.AnalyticsEvent;
import com.sanele.portfolio.analytics.entity.AnalyticsSession;
import com.sanele.portfolio.analytics.repository.AnalyticsEventRepository;
import com.sanele.portfolio.analytics.repository.AnalyticsSessionRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnalyticsService {
    private static final int MAXIMUM_RANGE_DAYS = 366;
    private final AnalyticsEventRepository eventRepository;
    private final AnalyticsSessionRepository sessionRepository;

    public AnalyticsService(AnalyticsEventRepository eventRepository, AnalyticsSessionRepository sessionRepository) {
        this.eventRepository = eventRepository;
        this.sessionRepository = sessionRepository;
    }

    @Transactional
    public void record(AnalyticsEventRequest request) {
        AnalyticsSession session = sessionRepository.findById(request.sessionId())
                .orElseGet(() -> sessionRepository.save(new AnalyticsSession(request.sessionId())));
        eventRepository.save(new AnalyticsEvent(session, request.eventName().value(), request.path()));
    }

    @Transactional(readOnly = true)
    public AnalyticsDashboardResponse getDashboard(LocalDate requestedFrom, LocalDate requestedTo) {
        LocalDate to = requestedTo == null ? LocalDate.now(ZoneOffset.UTC) : requestedTo;
        LocalDate from = requestedFrom == null ? to.minusDays(29) : requestedFrom;
        if (from.isAfter(to)) {
            throw new AnalyticsDateRangeException("The start date must be on or before the end date.");
        }
        if (ChronoUnit.DAYS.between(from, to) >= MAXIMUM_RANGE_DAYS) {
            throw new AnalyticsDateRangeException("Choose a date range of 366 days or fewer.");
        }

        Instant start = from.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant end = to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        long contactOpens = eventRepository.countEvents(AnalyticsEventType.CONTACT_FORM_OPEN.value(), start, end);
        long contactSubmissions = eventRepository.countEvents(
                AnalyticsEventType.CONTACT_FORM_SUBMIT.value(), start, end);
        long sessionsWithFormOpen = eventRepository.countDistinctSessions(
                AnalyticsEventType.CONTACT_FORM_OPEN.value(), start, end);
        long sessionsWithSubmission = eventRepository.countDistinctSessions(
                AnalyticsEventType.CONTACT_FORM_SUBMIT.value(), start, end);
        double conversionRate = sessionsWithFormOpen == 0 ? 0
                : Math.round((double) sessionsWithSubmission * 10000 / sessionsWithFormOpen) / 100.0;

        Map<String, Long> eventCounts = eventRepository.countEventsByType(start, end).stream()
                .collect(Collectors.toMap(
                        row -> row.getEventName(),
                        row -> row.getCount(),
                        Long::sum));
        List<AnalyticsDashboardResponse.EventCount> allEventCounts = Arrays.stream(AnalyticsEventType.values())
                .map(event -> new AnalyticsDashboardResponse.EventCount(event.value(),
                        eventCounts.getOrDefault(event.value(), 0L)))
                .toList();
        Map<LocalDate, Long> visitsByDate = eventRepository.findDailyVisits(start, end).stream()
                .collect(Collectors.toMap(row -> row.getVisitDate(), row -> row.getVisits()));

        return new AnalyticsDashboardResponse(
                from,
                to,
                eventRepository.countDistinctSessions(AnalyticsEventType.PAGE_VIEW.value(), start, end),
                eventRepository.countEvents(AnalyticsEventType.PAGE_VIEW.value(), start, end),
                eventRepository.countEvents(AnalyticsEventType.RESUME_VIEW.value(), start, end),
                eventRepository.countEvents(AnalyticsEventType.RESUME_DOWNLOAD.value(), start, end),
                contactOpens,
                contactSubmissions,
                conversionRate,
                from.datesUntil(to.plusDays(1))
                        .map(date -> new AnalyticsDashboardResponse.DailyVisit(date, visitsByDate.getOrDefault(date, 0L)))
                        .toList(),
                eventRepository.findTopPages(start, end).stream().limit(10)
                        .map(row -> new AnalyticsDashboardResponse.RankedPage(row.getPath(), row.getViews()))
                        .toList(),
                eventRepository.findTopProjects(start, end).stream().limit(10)
                        .map(row -> new AnalyticsDashboardResponse.RankedProject(row.getPath(), row.getViews()))
                        .toList(),
                allEventCounts);
    }
}
