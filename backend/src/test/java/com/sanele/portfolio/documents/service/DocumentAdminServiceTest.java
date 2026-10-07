package com.sanele.portfolio.documents.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanele.portfolio.auth.security.PortfolioUserDetails;
import com.sanele.portfolio.audit.entity.AuditLog;
import com.sanele.portfolio.audit.repository.AuditLogRepository;
import com.sanele.portfolio.common.exception.ResourceNotFoundException;
import com.sanele.portfolio.documents.dto.DocumentWriteRequest;
import com.sanele.portfolio.documents.entity.Document;
import com.sanele.portfolio.documents.repository.DocumentRepository;
import com.sanele.portfolio.users.entity.User;
import com.sanele.portfolio.users.repository.UserRepository;
import jakarta.validation.Validation;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DocumentAdminServiceTest {
    private static final PortfolioUserDetails ACTOR =
            new PortfolioUserDetails(5L, "admin", "admin@example.test", "Admin", "", true, List.of());
    @Mock private DocumentRepository documentRepository;
    @Mock private UserRepository userRepository;
    @Mock private AuditLogRepository auditLogRepository;
    private DocumentAdminService service;

    @BeforeEach
    void setUp() {
        service = new DocumentAdminService(documentRepository, userRepository,
                auditLogRepository, new ObjectMapper().findAndRegisterModules());
    }

    @Test
    void createDocumentPersistsMetadataAndAuditEntry() {
        when(userRepository.getReferenceById(ACTOR.getId())).thenReturn(mock(User.class));
        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.createDocument(request(" Sanele CV ", " https://example.test/cv.pdf "), ACTOR);

        assertEquals("Sanele CV", response.name());
        assertEquals("https://example.test/cv.pdf", response.storageUrl());
        assertEquals(32000L, response.sizeBytes());
        ArgumentCaptor<AuditLog> auditCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditCaptor.capture());
        assertEquals("CREATE", auditCaptor.getValue().getAction());
        assertEquals("DOCUMENT", auditCaptor.getValue().getEntityType());
        assertEquals("Sanele CV", auditCaptor.getValue().getDetails().get("after").get("name").asText());
    }

    @Test
    void updateDocumentRecordsBeforeAndAfterSnapshots() {
        Document document = new Document("Old CV", "CV", "https://example.test/old.pdf",
                "application/pdf", 100L, mock(User.class));
        when(documentRepository.findById(9L)).thenReturn(Optional.of(document));
        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.getReferenceById(ACTOR.getId())).thenReturn(mock(User.class));

        var response = service.updateDocument(9L, request("Updated CV", "https://example.test/new.pdf"), ACTOR);

        assertEquals("Updated CV", response.name());
        ArgumentCaptor<AuditLog> auditCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditCaptor.capture());
        assertEquals("Old CV", auditCaptor.getValue().getDetails().get("before").get("name").asText());
        assertEquals("Updated CV", auditCaptor.getValue().getDetails().get("after").get("name").asText());
    }

    @Test
    void deleteDocumentAuditsTheRemovedDocument() {
        Document document = new Document("Sanele CV", "CV", "https://example.test/cv.pdf",
                "application/pdf", 32000L, mock(User.class));
        when(documentRepository.findById(9L)).thenReturn(Optional.of(document));
        when(userRepository.getReferenceById(ACTOR.getId())).thenReturn(mock(User.class));

        service.deleteDocument(9L, ACTOR);

        verify(documentRepository).delete(document);
        ArgumentCaptor<AuditLog> auditCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditCaptor.capture());
        assertEquals("DELETE", auditCaptor.getValue().getAction());
        assertEquals("Sanele CV", auditCaptor.getValue().getDetails().get("before").get("name").asText());
    }

    @Test
    void getDocumentReturnsNotFoundForMissingId() {
        when(documentRepository.findById(9L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getDocument(9L));
    }

    @Test
    void writeRequestValidatesNamesUrlsAndFileSize() {
        try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
            var validator = validatorFactory.getValidator();
            assertTrue(validator.validate(request("CV", "https://example.test/cv.pdf")).isEmpty());
            assertFalse(validator.validate(new DocumentWriteRequest(" ", " ", "javascript:alert(1)",
                    "x".repeat(121), -1L)).isEmpty());
        }
    }

    private DocumentWriteRequest request(String name, String url) {
        return new DocumentWriteRequest(name, "CV", url, "application/pdf", 32000L);
    }
}
