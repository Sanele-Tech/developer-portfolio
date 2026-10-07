package com.sanele.portfolio.email.service;

import com.sanele.portfolio.common.exception.ConflictException;
import com.sanele.portfolio.common.exception.ResourceNotFoundException;
import com.sanele.portfolio.email.dto.EmailTemplateRequest;
import com.sanele.portfolio.email.dto.EmailTemplateResponse;
import com.sanele.portfolio.email.entity.EmailTemplate;
import com.sanele.portfolio.email.repository.EmailTemplateRepository;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EmailTemplateService {
    private static final Set<String> REQUIRED_TEMPLATES = Set.of(
            "CONTACT_NOTIFICATION", "VISITOR_CONFIRMATION", "RECRUITER_RESPONSE",
            "APPLICATION_FOLLOW_UP", "PROFESSIONAL_ENQUIRY");
    private final EmailTemplateRepository repository;

    public EmailTemplateService(EmailTemplateRepository repository) {
        this.repository = repository;
    }

    public List<EmailTemplateResponse> getTemplates() {
        return repository.findAll().stream()
                .sorted((left, right) -> left.getName().compareToIgnoreCase(right.getName()))
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public EmailTemplateResponse create(EmailTemplateRequest request) {
        if (repository.findByName(request.name()).isPresent()) {
            throw new ConflictException("An email template with that name already exists.");
        }
        return toResponse(repository.save(new EmailTemplate(request.name(), request.subject(), request.body())));
    }

    @Transactional
    public EmailTemplateResponse update(Long id, EmailTemplateRequest request) {
        EmailTemplate template = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Email template", id.toString()));
        if (REQUIRED_TEMPLATES.contains(template.getName())
                && !template.getName().equals(request.name())) {
            throw new ConflictException("Built-in communication template names cannot be changed.");
        }
        repository.findByName(request.name())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> { throw new ConflictException("An email template with that name already exists."); });
        template.update(request.name(), request.subject(), request.body());
        return toResponse(repository.save(template));
    }

    @Transactional
    public void delete(Long id) {
        EmailTemplate template = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Email template", id.toString()));
        if (REQUIRED_TEMPLATES.contains(template.getName())) {
            throw new ConflictException("Built-in communication templates cannot be deleted.");
        }
        repository.delete(template);
    }

    private EmailTemplateResponse toResponse(EmailTemplate template) {
        return new EmailTemplateResponse(template.getId(), template.getName(), template.getSubject(),
                template.getBody(), template.getCreatedAt(), template.getUpdatedAt());
    }
}
