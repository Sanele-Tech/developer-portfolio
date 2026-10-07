package com.sanele.portfolio.analytics.entity;

import com.sanele.portfolio.users.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "analytics_sessions")
public class AnalyticsSession {
    @Id private UUID id;
    @Column(name = "visitor_id", length = 120) private String visitorId;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id") private User user;
    @CreationTimestamp @Column(name = "started_at", nullable = false, updatable = false) private Instant startedAt;
    @Column(name = "ended_at") private Instant endedAt;

    protected AnalyticsSession() { }

    public AnalyticsSession(UUID id) {
        this.id = id;
    }

    public UUID getId() { return id; }
    public String getVisitorId() { return visitorId; }
    public User getUser() { return user; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getEndedAt() { return endedAt; }
}