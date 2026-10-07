package com.sanele.portfolio.documents.entity;

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
@Table(name = "documents")
public class Document {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 255) private String name;
    @Column(name = "document_type", nullable = false, length = 80) private String documentType;
    @Column(name = "storage_url", nullable = false, length = 500) private String storageUrl;
    @Column(name = "mime_type", length = 120) private String mimeType;
    @Column(name = "size_bytes") private Long sizeBytes;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "uploaded_by") private User uploadedBy;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;

    protected Document() { }

    public Document(String name, String documentType, String storageUrl,
            String mimeType, Long sizeBytes, User uploadedBy) {
        updateDetails(name, documentType, storageUrl, mimeType, sizeBytes);
        this.uploadedBy = uploadedBy;
    }

    public void updateDetails(String name, String documentType, String storageUrl,
            String mimeType, Long sizeBytes) {
        this.name = name;
        this.documentType = documentType;
        this.storageUrl = storageUrl;
        this.mimeType = mimeType;
        this.sizeBytes = sizeBytes;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDocumentType() { return documentType; }
    public String getStorageUrl() { return storageUrl; }
    public String getMimeType() { return mimeType; }
    public Long getSizeBytes() { return sizeBytes; }
    public User getUploadedBy() { return uploadedBy; }
    public Instant getCreatedAt() { return createdAt; }
}