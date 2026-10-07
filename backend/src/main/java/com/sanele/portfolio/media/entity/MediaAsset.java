package com.sanele.portfolio.media.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "media_assets")
public class MediaAsset {
    @Id
    @Column(name = "storage_key", length = 40)
    private String storageKey;

    @Column(name = "mime_type", nullable = false, length = 40)
    private String mimeType;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @Column(nullable = false, columnDefinition = "bytea")
    private byte[] content;

    protected MediaAsset() { }

    public MediaAsset(String storageKey, String mimeType, byte[] content) {
        this.storageKey = storageKey;
        this.mimeType = mimeType;
        this.content = content.clone();
        this.sizeBytes = content.length;
    }

    public String getStorageKey() { return storageKey; }
    public String getMimeType() { return mimeType; }
    public long getSizeBytes() { return sizeBytes; }
    public byte[] getContent() { return content.clone(); }
}
