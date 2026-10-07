package com.sanele.portfolio.achievements.entity;

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
@Table(name = "achievements")
public class Achievement {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "profile_id", nullable = false) private Profile profile;
    @Column(nullable = false, length = 200) private String title;
    @Column(name = "card_type", nullable = false, length = 30) private String cardType;
    @Column(length = 160) private String issuer;
    @Column(columnDefinition = "TEXT") private String description;
    @Column(name = "achieved_on") private LocalDate achievedOn;
    @Column(length = 500) private String url;
    @Column(name = "image_url", length = 500) private String imageUrl;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "image_urls", columnDefinition = "jsonb", nullable = false)
    private List<String> imageUrls = new ArrayList<>();
    @Column(name = "sort_order", nullable = false) private int sortOrder;

    protected Achievement() { }

    public Achievement(Profile profile, String title, String cardType, String issuer, String description,
            LocalDate achievedOn, String url, String imageUrl, List<String> imageUrls, int sortOrder) {
        updateDetails(profile, title, cardType, issuer, description, achievedOn, url, imageUrl, imageUrls, sortOrder);
    }

    public void updateDetails(Profile profile, String title, String cardType, String issuer, String description,
            LocalDate achievedOn, String url, String imageUrl, List<String> imageUrls, int sortOrder) {
        this.profile = profile;
        this.title = title;
        this.cardType = cardType;
        this.issuer = issuer;
        this.description = description;
        this.achievedOn = achievedOn;
        this.url = url;
        this.imageUrl = imageUrl;
        this.imageUrls = imageUrls == null ? new ArrayList<>() : new ArrayList<>(imageUrls);
        this.sortOrder = sortOrder;
    }

    public Long getId() { return id; }
    public Profile getProfile() { return profile; }
    public String getTitle() { return title; }
    public String getCardType() { return cardType; }
    public String getIssuer() { return issuer; }
    public String getDescription() { return description; }
    public LocalDate getAchievedOn() { return achievedOn; }
    public String getUrl() { return url; }
    public String getImageUrl() { return imageUrl; }
    public List<String> getImageUrls() {
        if (imageUrls == null || imageUrls.isEmpty()) return imageUrl == null ? List.of() : List.of(imageUrl);
        return List.copyOf(imageUrls);
    }
    public int getSortOrder() { return sortOrder; }
}