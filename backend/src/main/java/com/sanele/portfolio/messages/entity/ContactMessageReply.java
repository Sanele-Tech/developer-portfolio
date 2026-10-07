package com.sanele.portfolio.messages.entity;

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
@Table(name = "contact_message_replies")
public class ContactMessageReply {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "contact_message_id", nullable = false)
    private ContactMessage contactMessage;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "author_id") private User author;
    @Column(nullable = false, columnDefinition = "TEXT") private String body;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;

    protected ContactMessageReply() { }

    public ContactMessageReply(ContactMessage contactMessage, User author, String body) {
        this.contactMessage = contactMessage;
        this.author = author;
        this.body = body;
    }

    public Long getId() { return id; }
    public ContactMessage getContactMessage() { return contactMessage; }
    public User getAuthor() { return author; }
    public String getBody() { return body; }
    public Instant getCreatedAt() { return createdAt; }
}
