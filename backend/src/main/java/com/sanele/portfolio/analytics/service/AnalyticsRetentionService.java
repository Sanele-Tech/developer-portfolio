package com.sanele.portfolio.analytics.service;

import com.sanele.portfolio.analytics.repository.AnalyticsEventRepository;
import com.sanele.portfolio.analytics.repository.AnalyticsSessionRepository;
import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnalyticsRetentionService {
    private static final Logger log = LoggerFactory.getLogger(AnalyticsRetentionService.class);
    private final AnalyticsEventRepository eventRepository;
    private final AnalyticsSessionRepository sessionRepository;
    private static final Duration RETENTION = Duration.ofDays(180);

    public AnalyticsRetentionService(AnalyticsEventRepository eventRepository,
            AnalyticsSessionRepository sessionRepository) {
        this.eventRepository = eventRepository;
        this.sessionRepository = sessionRepository;
    }

    @Scheduled(cron = "0 15 3 * * *", zone = "UTC")
    @Transactional
    public void removeExpiredAnalytics() {
        Instant cutoff = Instant.now().minus(RETENTION);
        int eventsDeleted = eventRepository.deleteExpiredEvents(cutoff);
        int sessionsDeleted = sessionRepository.deleteExpiredEmptySessions(cutoff);
        if (eventsDeleted > 0 || sessionsDeleted > 0) {
            log.info("Removed {} expired analytics events and {} empty sessions.",
                    eventsDeleted, sessionsDeleted);
        }
    }
}
