package com.sanele.portfolio.analytics.repository;

import com.sanele.portfolio.analytics.entity.AnalyticsSession;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnalyticsSessionRepository extends JpaRepository<AnalyticsSession, UUID> {
    long countByStartedAtAfter(Instant since);

    @Modifying
    @Query(value = """
            delete from analytics_sessions s
            where s.started_at < :cutoff
              and not exists (select 1 from analytics_events e where e.session_id = s.id)
            """, nativeQuery = true)
    int deleteExpiredEmptySessions(@Param("cutoff") Instant cutoff);
}