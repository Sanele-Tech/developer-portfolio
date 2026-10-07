package com.sanele.portfolio.analytics.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sanele.portfolio.analytics.dto.AnalyticsEventRequest;
import com.sanele.portfolio.analytics.dto.AnalyticsEventType;
import com.sanele.portfolio.analytics.entity.AnalyticsEvent;
import com.sanele.portfolio.analytics.entity.AnalyticsSession;
import com.sanele.portfolio.analytics.repository.AnalyticsEventCount;
import com.sanele.portfolio.analytics.repository.AnalyticsEventRepository;
import com.sanele.portfolio.analytics.repository.AnalyticsPathCount;
import com.sanele.portfolio.analytics.repository.AnalyticsSessionRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {
    @Mock private AnalyticsEventRepository eventRepository;
    @Mock private AnalyticsSessionRepository sessionRepository;
    private AnalyticsService service;

    @BeforeEach
    void setUp() {
        service = new AnalyticsService(eventRepository, sessionRepository);
    }

    @Test
    void recordsOnlyWhitelistedEventForAnonymousSession() {
        UUID sessionId = UUID.randomUUID();
        AnalyticsEventRequest request = new AnalyticsEventRequest(
                sessionId, AnalyticsEventType.PAGE_VIEW, "/projects/example");
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        service.record(request);

        ArgumentCaptor<AnalyticsSession> sessionCaptor = ArgumentCaptor.forClass(AnalyticsSession.class);
        verify(sessionRepository).save(sessionCaptor.capture());
        assertEquals(sessionId, sessionCaptor.getValue().getId());
        ArgumentCaptor<AnalyticsEvent> eventCaptor = ArgumentCaptor.forClass(AnalyticsEvent.class);
        verify(eventRepository).save(eventCaptor.capture());
        assertEquals("page_view", eventCaptor.getValue().getEventName());
        assertEquals("/projects/example", eventCaptor.getValue().getPath());
        verify(sessionRepository).findById(sessionId);
    }

    @Test
    void rejectsReversedAndExcessiveDateRangesBeforeQuerying() {
        assertThrows(AnalyticsDateRangeException.class,
                () -> service.getDashboard(LocalDate.parse("2025-02-02"), LocalDate.parse("2025-02-01")));
        assertThrows(AnalyticsDateRangeException.class,
                () -> service.getDashboard(LocalDate.parse("2024-01-01"), LocalDate.parse("2025-01-01")));
        verify(eventRepository, never()).countEvents(anyString(), any(Instant.class), any(Instant.class));
    }

    @Test
    void aggregatesDashboardAndCalculatesContactConversionFromSessions() {
        LocalDate from = LocalDate.parse("2025-05-01");
        LocalDate to = LocalDate.parse("2025-05-02");
        when(eventRepository.countEvents(anyString(), any(Instant.class), any(Instant.class))).thenReturn(0L);
        when(eventRepository.countEvents(eq("page_view"), any(Instant.class), any(Instant.class))).thenReturn(10L);
        when(eventRepository.countDistinctSessions(eq("page_view"), any(Instant.class), any(Instant.class))).thenReturn(7L);
        when(eventRepository.countEvents(eq("contact_form_open"), any(Instant.class), any(Instant.class))).thenReturn(4L);
        when(eventRepository.countEvents(eq("contact_form_submit"), any(Instant.class), any(Instant.class))).thenReturn(2L);
        when(eventRepository.countDistinctSessions(eq("contact_form_open"), any(Instant.class), any(Instant.class))).thenReturn(4L);
        when(eventRepository.countDistinctSessions(eq("contact_form_submit"), any(Instant.class), any(Instant.class))).thenReturn(1L);
        when(eventRepository.findDailyVisits(any(Instant.class), any(Instant.class))).thenReturn(List.of());
        when(eventRepository.findTopPages(any(Instant.class), any(Instant.class))).thenReturn(List.<AnalyticsPathCount>of());
        when(eventRepository.findTopProjects(any(Instant.class), any(Instant.class))).thenReturn(List.<AnalyticsPathCount>of());
        when(eventRepository.countEventsByType(any(Instant.class), any(Instant.class))).thenReturn(List.<AnalyticsEventCount>of());

        var dashboard = service.getDashboard(from, to);

        assertEquals(7L, dashboard.visits());
        assertEquals(10L, dashboard.pageViews());
        assertEquals(4L, dashboard.contactFormOpens());
        assertEquals(2L, dashboard.contactFormSubmissions());
        assertEquals(25.0, dashboard.contactConversionRate());
        assertEquals(from, dashboard.from());
        assertEquals(to, dashboard.to());
    }
}
