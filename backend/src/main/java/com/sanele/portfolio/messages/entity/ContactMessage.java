package com.sanele.portfolio.messages.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "contact_messages")
public class ContactMessage {
    public enum Status { NEW, READ, REPLIED, ARCHIVED }
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 120) private String name;
    @Column(nullable = false, length = 320) private String email;
    @Column(length = 160) private String company;
    @Column(length = 200) private String subject;
    @Column(nullable = false, columnDefinition = "TEXT") private String message;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private Status status = Status.NEW;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;

    protected ContactMessage() { }

    public ContactMessage(String name, String email, String company, String subject, String message) {
        this.name = name;
        this.email = email;
        this.company = company;
        this.subject = subject;
        this.message = message;
    }

    public ContactMessage(String name, String email, String subject, String message) {
        this(name, email, null, subject, message);
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        if (status == null) status = Status.NEW;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getCompany() { return company; }
    public String getSubject() { return subject; }
    public String getMessage() { return message; }
    public Status getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public void updateStatus(Status status) { this.status = status; }
}