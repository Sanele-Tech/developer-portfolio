package com.sanele.portfolio.content.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "seo_metadata")
public class SeoMetadata {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "page_id", unique = true) private Page page;
    @Column(name = "meta_title", length = 200) private String metaTitle;
    @Column(name = "meta_description", length = 500) private String metaDescription;
    @Column(name = "canonical_url", length = 500) private String canonicalUrl;
    @Column(name = "og_image_url", length = 500) private String ogImageUrl;
    @UpdateTimestamp @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    public Long getId() { return id; }
    public Page getPage() { return page; }
    public String getMetaTitle() { return metaTitle; }
    public String getMetaDescription() { return metaDescription; }
    public String getCanonicalUrl() { return canonicalUrl; }
    public String getOgImageUrl() { return ogImageUrl; }
}