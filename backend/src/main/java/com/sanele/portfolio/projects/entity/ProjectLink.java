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
@Table(name = "project_links")
public class ProjectLink {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "project_id", nullable = false) private Project project;
    @Column(nullable = false, length = 80) private String label;
    @Column(nullable = false, length = 500) private String url;
    @Column(name = "link_type", nullable = false, length = 40) private String linkType = "OTHER";
    protected ProjectLink() { }
    public ProjectLink(Project project, String label, String url, String linkType) {
        this.project = project;
        this.label = label;
        this.url = url;
        this.linkType = linkType;
    }
    public Long getId() { return id; }
    public Project getProject() { return project; }
    public String getLabel() { return label; }
    public String getUrl() { return url; }
    public String getLinkType() { return linkType; }
}