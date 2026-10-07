package com.sanele.portfolio.messages.entity;

import com.sanele.portfolio.common.entity.AuditedEntity;
import com.sanele.portfolio.users.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "messages")
public class Message extends AuditedEntity {
    @Column(name = "sender_name", nullable = false, length = 120) private String senderName;
    @Column(name = "sender_email", nullable = false, length = 320) private String senderEmail;
    @Column(length = 200) private String subject;
    @Column(nullable = false, columnDefinition = "TEXT") private String body;
    @Column(nullable = false, length = 30) private String status = "NEW";
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "assigned_to") private User assignedTo;
    public String getSenderName() { return senderName; }
    public String getSenderEmail() { return senderEmail; }
    public String getSubject() { return subject; }
    public String getBody() { return body; }
    public String getStatus() { return status; }
    public User getAssignedTo() { return assignedTo; }
}