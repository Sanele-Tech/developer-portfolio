package com.sanele.portfolio.content.entity;

import com.sanele.portfolio.common.entity.AuditedEntity;
import com.sanele.portfolio.users.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "pages")
public class Page extends AuditedEntity {
    public enum Status { DRAFT, PUBLISHED, ARCHIVED }
    @Column(nullable = false, unique = true, length = 180) private String slug;
    @Column(nullable = false, length = 200) private String title;
    @Column(columnDefinition = "TEXT") private String excerpt;
    @Column(nullable = false, columnDefinition = "TEXT") private String content;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private Status status = Status.DRAFT;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "author_id") private User author;

    protected Page() { }

    public Page(String slug, String title, String excerpt, String content, Status status, User author) {
        updateDetails(slug, title, excerpt, content, status, author);
    }

    public void updateDetails(String slug, String title, String excerpt, String content, Status status, User author) {
        this.slug = slug;
        this.title = title;
        this.excerpt = excerpt;
        this.content = content;
        this.status = status;
        this.author = author;
    }

    public String getSlug() { return slug; }
    public String getTitle() { return title; }
    public String getExcerpt() { return excerpt; }
    public String getContent() { return content; }
    public Status getStatus() { return status; }
    public User getAuthor() { return author; }
}