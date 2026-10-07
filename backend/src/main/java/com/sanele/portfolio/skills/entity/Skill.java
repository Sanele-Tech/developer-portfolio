package com.sanele.portfolio.skills.entity;

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

@Entity
@Table(name = "skills")
public class Skill {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "profile_id", nullable = false) private Profile profile;
    @Column(nullable = false, length = 120) private String name;
    @Column(length = 120) private String category;
    @Column private Short proficiency;
    @Column(name = "sort_order", nullable = false) private int sortOrder;

    protected Skill() { }

    public Skill(Profile profile, String name, String category, Short proficiency, int sortOrder) {
        updateDetails(profile, name, category, proficiency, sortOrder);
    }

    public void updateDetails(Profile profile, String name, String category, Short proficiency, int sortOrder) {
        this.profile = profile;
        this.name = name;
        this.category = category;
        this.proficiency = proficiency;
        this.sortOrder = sortOrder;
    }

    public Long getId() { return id; }
    public Profile getProfile() { return profile; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public Short getProficiency() { return proficiency; }
    public int getSortOrder() { return sortOrder; }
}