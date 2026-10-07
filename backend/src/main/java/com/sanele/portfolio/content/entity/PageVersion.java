package com.sanele.portfolio.content.entity;

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
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "page_versions")
public class PageVersion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "page_id", nullable = false) private Page page;
    @Column(name = "version_number", nullable = false) private int versionNumber;
    @Column(nullable = false, columnDefinition = "TEXT") private String content;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "author_id") private User author;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;

    protected PageVersion() { }

    public PageVersion(Page page, int versionNumber, String content, User author) {
        this.page = page;
        this.versionNumber = versionNumber;
        this.content = content;
        this.author = author;
    }

    public Long getId() { return id; }
    public Page getPage() { return page; }
    public int getVersionNumber() { return versionNumber; }
    public String getContent() { return content; }
    public User getAuthor() { return author; }
    public Instant getCreatedAt() { return createdAt; }
}