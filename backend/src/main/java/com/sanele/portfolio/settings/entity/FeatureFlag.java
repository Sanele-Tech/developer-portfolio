package com.sanele.portfolio.settings.entity;

import com.sanele.portfolio.users.entity.User;
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
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "feature_flags")
public class FeatureFlag {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "flag_key", nullable = false, unique = true, length = 160) private String flagKey;
    @Column(nullable = false) private boolean enabled;
    @Column(length = 500) private String description;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "updated_by") private User updatedBy;
    @UpdateTimestamp @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    public Long getId() { return id; }
    public String getFlagKey() { return flagKey; }
    public boolean isEnabled() { return enabled; }
    public String getDescription() { return description; }
    public User getUpdatedBy() { return updatedBy; }
    public Instant getUpdatedAt() { return updatedAt; }
}