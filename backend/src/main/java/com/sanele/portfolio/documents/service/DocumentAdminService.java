package com.sanele.portfolio.documents.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanele.portfolio.auth.security.PortfolioUserDetails;
import com.sanele.portfolio.audit.entity.AuditLog;
import com.sanele.portfolio.audit.repository.AuditLogRepository;
import com.sanele.portfolio.common.exception.ResourceNotFoundException;
import com.sanele.portfolio.documents.dto.DocumentResponse;
import com.sanele.portfolio.documents.dto.DocumentWriteRequest;
import com.sanele.portfolio.documents.entity.Document;
import com.sanele.portfolio.documents.repository.DocumentRepository;
import com.sanele.portfolio.users.repository.UserRepository;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentAdminService {
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public DocumentAdminService(DocumentRepository documentRepository, UserRepository userRepository,
            AuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> getDocuments() {
        return documentRepository.findAllByOrderByCreatedAtDescIdDesc().stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public DocumentResponse getDocument(Long id) {
        return toResponse(findDocument(id));
    }

    @Transactional
    public DocumentResponse createDocument(DocumentWriteRequest request, PortfolioUserDetails actor) {
        Document document = new Document(request.name().trim(), request.documentType().trim(),
                request.storageUrl().trim(), normalizeOptional(request.mimeType()), request.sizeBytes(),
                userRepository.getReferenceById(actor.getId()));
        documentRepository.save(document);
        writeAudit(actor, "CREATE", document, null, snapshot(document));
        return toResponse(document);
    }

    @Transactional
    public DocumentResponse updateDocument(Long id, DocumentWriteRequest request, PortfolioUserDetails actor) {
        Document document = findDocument(id);
        Map<String, Object> before = snapshot(document);
        document.updateDetails(request.name().trim(), request.documentType().trim(), request.storageUrl().trim(),
                normalizeOptional(request.mimeType()), request.sizeBytes());
        documentRepository.save(document);
        writeAudit(actor, "UPDATE", document, before, snapshot(document));
        return toResponse(document);
    }

    @Transactional
    public void deleteDocument(Long id, PortfolioUserDetails actor) {
        Document document = findDocument(id);
        writeAudit(actor, "DELETE", document, snapshot(document), null);
        documentRepository.delete(document);
    }

    private Document findDocument(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document", id.toString()));
    }

    private DocumentResponse toResponse(Document document) {
        return new DocumentResponse(document.getId(), document.getName(), document.getDocumentType(),
                document.getStorageUrl(), document.getMimeType(), document.getSizeBytes(), document.getCreatedAt());
    }

    private Map<String, Object> snapshot(Document document) {
        return Map.of(
                "name", document.getName(),
                "documentType", document.getDocumentType(),
                "storageUrl", document.getStorageUrl(),
                "mimeType", document.getMimeType() == null ? "" : document.getMimeType(),
                "sizeBytes", document.getSizeBytes() == null ? "" : document.getSizeBytes());
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private void writeAudit(PortfolioUserDetails actor, String action, Document document,
            Map<String, Object> before, Map<String, Object> after) {
        Map<String, Object> details = Map.of(
                "before", before == null ? Map.of() : before,
                "after", after == null ? Map.of() : after);
        auditLogRepository.save(new AuditLog(userRepository.getReferenceById(actor.getId()),
                action, "DOCUMENT", document.getId(), objectMapper.valueToTree(details)));
    }
}
