package com.sanele.portfolio.projects.entity;

import com.sanele.portfolio.common.entity.AuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "projects")
public class Project extends AuditedEntity {
    @Column(nullable = false, length = 160) private String title;
    @Column(nullable = false, unique = true, length = 180) private String slug;
    @Column(nullable = false, columnDefinition = "TEXT") private String description;
    @Column(nullable = false, columnDefinition = "TEXT") private String technologies;
    @Column(name = "repository_url", length = 500) private String repositoryUrl;
    @Column(name = "live_url", length = 500) private String liveUrl;
    @Column(name = "image_url", length = 500) private String imageUrl;
    @Column(nullable = false) private boolean featured;
    @Column(name = "sort_order", nullable = false) private int sortOrder;
    @ManyToMany
    @JoinTable(name = "project_technologies", joinColumns = @JoinColumn(name = "project_id"), inverseJoinColumns = @JoinColumn(name = "technology_id"))
    private Set<Technology> technologyEntities = new HashSet<>();
    public String getTitle() { return title; }
    public String getSlug() { return slug; }
    public String getDescription() { return description; }
    public String getTechnologies() { return technologies; }
    public String getRepositoryUrl() { return repositoryUrl; }
    public String getLiveUrl() { return liveUrl; }
    public String getImageUrl() { return imageUrl; }
    public boolean isFeatured() { return featured; }
    public int getSortOrder() { return sortOrder; }
    public Set<Technology> getTechnologyEntities() { return technologyEntities; }

    public void updateContent(String title, String slug, String description, String technologies,
            String repositoryUrl, String liveUrl, String imageUrl, boolean featured, int sortOrder) {
        this.title = title;
        this.slug = slug;
        this.description = description;
        this.technologies = technologies;
        this.repositoryUrl = repositoryUrl;
        this.liveUrl = liveUrl;
        this.imageUrl = imageUrl;
        this.featured = featured;
        this.sortOrder = sortOrder;
    }
}