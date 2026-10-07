package com.sanele.portfolio.education.entity;

import com.sanele.portfolio.profile.entity.Profile;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
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
@Table(name = "education")
public class Education {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "profile_id", nullable = false) private Profile profile;
    @Column(nullable = false, length = 200) private String institution;
    @Column(nullable = false, length = 200) private String qualification;
    @Column(columnDefinition = "TEXT") private String description;
    @Column(name = "start_date") private LocalDate startDate;
    @Column(name = "end_date") private LocalDate endDate;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "core_modules", columnDefinition = "jsonb", nullable = false)
    private List<String> coreModules = new ArrayList<>();
    @Column(columnDefinition = "TEXT") private String experience;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "image_urls", columnDefinition = "jsonb", nullable = false)
    private List<String> imageUrls = new ArrayList<>();
    @Column(name = "sort_order", nullable = false) private int sortOrder;
    protected Education() { }

    public Education(Profile profile, String institution, String qualification, String description,
            LocalDate startDate, LocalDate endDate, List<String> coreModules, String experience,
            List<String> imageUrls, int sortOrder) {
        updateDetails(profile, institution, qualification, description, startDate, endDate,
                coreModules, experience, imageUrls, sortOrder);
    }

    public void updateDetails(Profile profile, String institution, String qualification, String description,
            LocalDate startDate, LocalDate endDate, List<String> coreModules, String experience,
            List<String> imageUrls, int sortOrder) {
        this.profile = profile;
        this.institution = institution;
        this.qualification = qualification;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.coreModules = coreModules == null ? new ArrayList<>() : new ArrayList<>(coreModules);
        this.experience = experience;
        this.imageUrls = imageUrls == null ? new ArrayList<>() : new ArrayList<>(imageUrls);
        this.sortOrder = sortOrder;
    }

    public Long getId() { return id; }
    public Profile getProfile() { return profile; }
    public String getInstitution() { return institution; }
    public String getQualification() { return qualification; }
    public String getDescription() { return description; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public List<String> getCoreModules() { return coreModules == null ? List.of() : List.copyOf(coreModules); }
    public String getExperience() { return experience; }
    public List<String> getImageUrls() { return imageUrls == null ? List.of() : List.copyOf(imageUrls); }
    public int getSortOrder() { return sortOrder; }
}