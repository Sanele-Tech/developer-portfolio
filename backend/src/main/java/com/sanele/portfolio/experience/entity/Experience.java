package com.sanele.portfolio.experience.entity;

import com.sanele.portfolio.profile.entity.Profile;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "experience")
public class Experience {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "profile_id", nullable = false) private Profile profile;
    @Column(name = "job_title", nullable = false, length = 160) private String jobTitle;
    @Column(nullable = false, length = 160) private String company;
    @Column(nullable = false, columnDefinition = "TEXT") private String description;
    @Column(name = "start_date", nullable = false) private LocalDate startDate;
    @Column(name = "end_date") private LocalDate endDate;
    @Column(name = "is_current_role", nullable = false) private boolean currentRole;
    @Column(name = "sort_order", nullable = false) private int sortOrder;
    protected Experience() { }

    public void updateDetails(Profile profile, String jobTitle, String company, String description,
            LocalDate startDate, LocalDate endDate, boolean currentRole, int sortOrder) {
        this.profile = profile;
        this.jobTitle = jobTitle;
        this.company = company;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.currentRole = currentRole;
        this.sortOrder = sortOrder;
    }

    public Experience(Profile profile, String jobTitle, String company, String description,
            LocalDate startDate, LocalDate endDate, boolean currentRole, int sortOrder) {
        updateDetails(profile, jobTitle, company, description, startDate, endDate, currentRole, sortOrder);
    }

    public Long getId() { return id; }
    public Profile getProfile() { return profile; }
    public String getJobTitle() { return jobTitle; }
    public String getCompany() { return company; }
    public String getDescription() { return description; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public boolean isCurrentRole() { return currentRole; }
    public int getSortOrder() { return sortOrder; }
}