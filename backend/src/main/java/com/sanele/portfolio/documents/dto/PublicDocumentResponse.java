package com.sanele.portfolio.documents.dto;

public record PublicDocumentResponse(String name, String storageUrl, String mimeType, Long sizeBytes) {
}
