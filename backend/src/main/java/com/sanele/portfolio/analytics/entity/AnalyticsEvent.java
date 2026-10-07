package com.sanele.portfolio.analytics.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "analytics_events")
public class AnalyticsEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "session_id", nullable = false) private AnalyticsSession session;
    @Column(name = "event_name", nullable = false, length = 120) private String eventName;
    @Column(length = 500) private String path;
    @JdbcTypeCode(SqlTypes.JSON) @Column(columnDefinition = "jsonb") private String metadata;
    @CreationTimestamp @Column(name = "occurred_at", nullable = false, updatable = false) private Instant occurredAt;

    protected AnalyticsEvent() { }

    public AnalyticsEvent(AnalyticsSession session, String eventName, String path) {
        this.session = session;
        this.eventName = eventName;
        this.path = path;
    }

    public Long getId() { return id; }
    public AnalyticsSession getSession() { return session; }
    public String getEventName() { return eventName; }
    public String getPath() { return path; }
    public String getMetadata() { return metadata; }
    public Instant getOccurredAt() { return occurredAt; }
}