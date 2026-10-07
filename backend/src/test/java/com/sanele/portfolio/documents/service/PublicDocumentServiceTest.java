package com.sanele.portfolio.documents.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.sanele.portfolio.common.exception.ResourceNotFoundException;
import com.sanele.portfolio.documents.entity.Document;
import com.sanele.portfolio.documents.repository.DocumentRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class PublicDocumentServiceTest {
    private final DocumentRepository documentRepository = mock(DocumentRepository.class);
    private final PublicDocumentService service = new PublicDocumentService(documentRepository);

    @Test
    void returnsOnlyPublicCvMetadataAndUrl() {
        Document document = mock(Document.class);
        when(documentRepository.findFirstByDocumentTypeIgnoreCaseOrderByCreatedAtDescIdDesc("CV"))
                .thenReturn(Optional.of(document));
        when(document.getName()).thenReturn("Sanele Dlomo CV");
        when(document.getStorageUrl()).thenReturn("https://files.example.test/cv.pdf");
        when(document.getMimeType()).thenReturn("application/pdf");
        when(document.getSizeBytes()).thenReturn(1234L);

        var response = service.getCurrentCv();

        assertEquals("Sanele Dlomo CV", response.name());
        assertEquals("https://files.example.test/cv.pdf", response.storageUrl());
        assertEquals("application/pdf", response.mimeType());
        assertEquals(1234L, response.sizeBytes());
    }

    @Test
    void missingCvIsNotFound() {
        when(documentRepository.findFirstByDocumentTypeIgnoreCaseOrderByCreatedAtDescIdDesc("CV"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, service::getCurrentCv);
    }
}
