package com.sanele.portfolio.documents.controller;

import com.sanele.portfolio.auth.security.PortfolioUserDetails;
import com.sanele.portfolio.documents.dto.DocumentResponse;
import com.sanele.portfolio.documents.dto.DocumentWriteRequest;
import com.sanele.portfolio.documents.service.DocumentAdminService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/documents")
@PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
public class DocumentAdminController {
    private final DocumentAdminService documentAdminService;

    public DocumentAdminController(DocumentAdminService documentAdminService) {
        this.documentAdminService = documentAdminService;
    }

    @GetMapping
    public List<DocumentResponse> getDocuments() {
        return documentAdminService.getDocuments();
    }

    @GetMapping("/{id}")
    public DocumentResponse getDocument(@PathVariable Long id) {
        return documentAdminService.getDocument(id);
    }

    @PostMapping
    public ResponseEntity<DocumentResponse> createDocument(@Valid @RequestBody DocumentWriteRequest request,
            @AuthenticationPrincipal PortfolioUserDetails actor) {
        DocumentResponse document = documentAdminService.createDocument(request, actor);
        return ResponseEntity.created(URI.create("/api/admin/documents/" + document.id())).body(document);
    }

    @PutMapping("/{id}")
    public DocumentResponse updateDocument(@PathVariable Long id,
            @Valid @RequestBody DocumentWriteRequest request,
            @AuthenticationPrincipal PortfolioUserDetails actor) {
        return documentAdminService.updateDocument(id, request, actor);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDocument(@PathVariable Long id, @AuthenticationPrincipal PortfolioUserDetails actor) {
        documentAdminService.deleteDocument(id, actor);
    }
}
