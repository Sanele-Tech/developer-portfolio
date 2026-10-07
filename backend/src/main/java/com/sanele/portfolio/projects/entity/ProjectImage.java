package com.sanele.portfolio.projects.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "project_images")
public class ProjectImage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "project_id", nullable = false) private Project project;
    @Column(name = "image_url", nullable = false, length = 500) private String imageUrl;
    @Column(name = "alt_text", length = 255) private String altText;
    @Column(length = 160) private String heading;
    @Column(columnDefinition = "TEXT") private String description;
    @Column(name = "sort_order", nullable = false) private int sortOrder;
    protected ProjectImage() { }
    public ProjectImage(Project project, String imageUrl, String altText, String heading,
            String description, int sortOrder) {
        this.project = project;
        this.imageUrl = imageUrl;
        this.altText = altText;
        this.heading = heading;
        this.description = description;
        this.sortOrder = sortOrder;
    }
    public Long getId() { return id; }
    public Project getProject() { return project; }
    public String getImageUrl() { return imageUrl; }
    public String getAltText() { return altText; }
    public String getHeading() { return heading; }
    public String getDescription() { return description; }
    public int getSortOrder() { return sortOrder; }
}