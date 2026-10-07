package com.sanele.portfolio.email.controller;

import com.sanele.portfolio.email.dto.EmailTemplateRequest;
import com.sanele.portfolio.email.dto.EmailTemplateResponse;
import com.sanele.portfolio.email.service.EmailTemplateService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/admin/email-templates")
@PreAuthorize("hasRole('ADMIN')")
public class EmailTemplateAdminController {
    private final EmailTemplateService templateService;

    public EmailTemplateAdminController(EmailTemplateService templateService) {
        this.templateService = templateService;
    }

    @GetMapping
    public List<EmailTemplateResponse> getTemplates() {
        return templateService.getTemplates();
    }

    @PostMapping
    public ResponseEntity<EmailTemplateResponse> create(@Valid @RequestBody EmailTemplateRequest request) {
        EmailTemplateResponse created = templateService.create(request);
        return ResponseEntity.created(URI.create("/api/admin/email-templates/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public EmailTemplateResponse update(@PathVariable Long id,
            @Valid @RequestBody EmailTemplateRequest request) {
        return templateService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        templateService.delete(id);
    }
}
