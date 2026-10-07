package com.sanele.portfolio.documents.service;

import com.sanele.portfolio.common.exception.ResourceNotFoundException;
import com.sanele.portfolio.documents.dto.PublicDocumentResponse;
import com.sanele.portfolio.documents.entity.Document;
import com.sanele.portfolio.documents.repository.DocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PublicDocumentService {
    private final DocumentRepository documentRepository;

    public PublicDocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public PublicDocumentResponse getCurrentCv() {
        Document document = documentRepository
                .findFirstByDocumentTypeIgnoreCaseOrderByCreatedAtDescIdDesc("CV")
                .orElseThrow(() -> new ResourceNotFoundException("CV", "current"));
        return new PublicDocumentResponse(document.getName(), document.getStorageUrl(),
                document.getMimeType(), document.getSizeBytes());
    }
}
