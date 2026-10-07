package com.sanele.portfolio.analytics.repository;

import com.sanele.portfolio.analytics.entity.AnalyticsEvent;
import com.sanele.portfolio.analytics.repository.AnalyticsDailyVisit;
import com.sanele.portfolio.analytics.repository.AnalyticsEventCount;
import com.sanele.portfolio.analytics.repository.AnalyticsPathCount;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnalyticsEventRepository extends JpaRepository<AnalyticsEvent, Long> {
    @Query("select count(e.id) from AnalyticsEvent e where e.eventName = :eventName "
            + "and e.occurredAt >= :from and e.occurredAt < :to")
    long countEvents(@Param("eventName") String eventName, @Param("from") Instant from, @Param("to") Instant to);

    @Query("select count(distinct e.session.id) from AnalyticsEvent e where e.eventName = :eventName "
            + "and e.occurredAt >= :from and e.occurredAt < :to")
    long countDistinctSessions(@Param("eventName") String eventName,
            @Param("from") Instant from, @Param("to") Instant to);

    @Query(value = """
            select cast(date_trunc('day', occurred_at at time zone 'UTC') as date) as visit_date,
                   count(distinct session_id) as visits
            from analytics_events
            where event_name = 'page_view' and occurred_at >= :from and occurred_at < :to
            group by 1
            order by 1
            """, nativeQuery = true)
    List<AnalyticsDailyVisit> findDailyVisits(@Param("from") Instant from, @Param("to") Instant to);

    @Query("""
            select e.path as path, count(e.id) as views
            from AnalyticsEvent e
            where e.eventName = 'page_view' and e.path not like '/projects/%'
              and e.occurredAt >= :from and e.occurredAt < :to
            group by e.path
            order by count(e.id) desc
            """)
    List<AnalyticsPathCount> findTopPages(@Param("from") Instant from, @Param("to") Instant to);

    @Query("""
            select e.path as path, count(e.id) as views
            from AnalyticsEvent e
            where e.eventName = 'project_view' and e.path like '/projects/%'
              and e.occurredAt >= :from and e.occurredAt < :to
            group by e.path
            order by count(e.id) desc
            """)
    List<AnalyticsPathCount> findTopProjects(@Param("from") Instant from, @Param("to") Instant to);

    @Query("""
            select e.eventName as eventName, count(e.id) as count
            from AnalyticsEvent e
            where e.occurredAt >= :from and e.occurredAt < :to
            group by e.eventName
            """)
    List<AnalyticsEventCount> countEventsByType(@Param("from") Instant from, @Param("to") Instant to);

    @Modifying
    @Query("delete from AnalyticsEvent e where e.occurredAt < :cutoff")
    int deleteExpiredEvents(@Param("cutoff") Instant cutoff);
}