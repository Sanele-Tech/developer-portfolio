package com.sanele.portfolio.documents.controller;

import com.sanele.portfolio.documents.dto.PublicDocumentResponse;
import com.sanele.portfolio.documents.service.PublicDocumentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/documents")
public class PublicDocumentController {
    private final PublicDocumentService publicDocumentService;

    public PublicDocumentController(PublicDocumentService publicDocumentService) {
        this.publicDocumentService = publicDocumentService;
    }

    @GetMapping("/cv/current")
    public PublicDocumentResponse getCurrentCv() {
        return publicDocumentService.getCurrentCv();
    }
}
