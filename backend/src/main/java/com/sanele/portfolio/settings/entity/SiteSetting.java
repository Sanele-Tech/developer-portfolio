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
@Table(name = "site_settings")
public class SiteSetting {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "setting_key", nullable = false, unique = true, length = 160) private String settingKey;
    @Column(name = "setting_value", columnDefinition = "TEXT") private String settingValue;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "updated_by") private User updatedBy;
    @UpdateTimestamp @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected SiteSetting() { }

    public SiteSetting(String settingKey, String settingValue, User updatedBy) {
        updateDetails(settingValue, updatedBy);
        this.settingKey = settingKey;
    }

    public void updateDetails(String settingValue, User updatedBy) {
        this.settingValue = settingValue;
        this.updatedBy = updatedBy;
    }

    public Long getId() { return id; }
    public String getSettingKey() { return settingKey; }
    public String getSettingValue() { return settingValue; }
    public User getUpdatedBy() { return updatedBy; }
    public Instant getUpdatedAt() { return updatedAt; }
}