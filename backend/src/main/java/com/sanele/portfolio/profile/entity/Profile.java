package com.sanele.portfolio.profile.entity;

import com.sanele.portfolio.common.entity.AuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "profile")
public class Profile extends AuditedEntity {
    @Column(nullable = false, length = 120) private String name;
    @Column(nullable = false, length = 160) private String title;
    @Column(nullable = false, columnDefinition = "TEXT") private String bio;
    @Column(nullable = false, length = 320) private String email;
    @Column(name = "phone_number", length = 40) private String phoneNumber;
    @Column(length = 160) private String location;
    @Column(name = "avatar_url", length = 500) private String avatarUrl;
    @Column(name = "github_url", length = 500) private String githubUrl;
    @Column(name = "linkedin_url", length = 500) private String linkedinUrl;

    public String getName() { return name; }
    public String getTitle() { return title; }
    public String getBio() { return bio; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getLocation() { return location; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getGithubUrl() { return githubUrl; }
    public String getLinkedinUrl() { return linkedinUrl; }

    public void updateDetails(String name, String title, String bio, String email, String phoneNumber, String location,
            String avatarUrl, String githubUrl, String linkedinUrl) {
        this.name = name;
        this.title = title;
        this.bio = bio;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.location = location;
        this.avatarUrl = avatarUrl;
        this.githubUrl = githubUrl;
        this.linkedinUrl = linkedinUrl;
    }
}