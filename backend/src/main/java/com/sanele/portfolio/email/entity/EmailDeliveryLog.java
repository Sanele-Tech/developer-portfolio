package com.sanele.portfolio.email.entity;

import com.sanele.portfolio.email.dto.EmailDeliveryStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "email_delivery_logs")
public class EmailDeliveryLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "contact_message_id") private Long contactMessageId;
    @Column(name = "template_name", nullable = false, length = 120) private String templateName;
    @Column(length = 320) private String recipient;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private EmailDeliveryStatus status;
    @Column(name = "failure_reason", length = 500) private String failureReason;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;

    protected EmailDeliveryLog() { }

    public EmailDeliveryLog(Long contactMessageId, String templateName, String recipient,
            EmailDeliveryStatus status, String failureReason) {
        this.contactMessageId = contactMessageId;
        this.templateName = templateName;
        this.recipient = recipient;
        this.status = status;
        this.failureReason = failureReason;
    }

    public Long getId() { return id; }
    public Long getContactMessageId() { return contactMessageId; }
    public String getTemplateName() { return templateName; }
    public String getRecipient() { return recipient; }
    public EmailDeliveryStatus getStatus() { return status; }
    public String getFailureReason() { return failureReason; }
    public Instant getCreatedAt() { return createdAt; }
}
