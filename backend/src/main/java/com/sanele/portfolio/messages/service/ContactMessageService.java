package com.sanele.portfolio.messages.service;

import com.sanele.portfolio.messages.dto.ContactRequest;
import com.sanele.portfolio.messages.dto.ContactResponse;
import com.sanele.portfolio.messages.entity.ContactMessage;
import com.sanele.portfolio.messages.repository.ContactMessageRepository;
import com.sanele.portfolio.email.service.EmailNotificationService;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ContactMessageService {
    private final ContactMessageRepository messageRepository;
    private final EmailNotificationService emailNotificationService;
    private final String notificationEmail;

    public ContactMessageService(ContactMessageRepository messageRepository,
            EmailNotificationService emailNotificationService,
            @Value("${portfolio.mail.notification-email:}") String notificationEmail) {
        this.messageRepository = messageRepository;
        this.emailNotificationService = emailNotificationService;
        this.notificationEmail = notificationEmail;
    }

    public Optional<ContactResponse> submit(ContactRequest request) {
        if (request.website() != null && !request.website().isBlank()) return Optional.empty();

        ContactMessage saved = messageRepository.save(new ContactMessage(request.name().trim(),
                request.email().trim(), normalize(request.company()), normalize(request.subject()),
                request.message().trim()));
        Map<String, String> values = Map.of(
                "name", saved.getName(),
                "email", saved.getEmail(),
                "company", saved.getCompany() == null ? "Not provided" : saved.getCompany(),
                "subject", sanitizeSubject(saved.getSubject()),
                "message", saved.getMessage());

        emailNotificationService.sendTemplate("CONTACT_NOTIFICATION", notificationEmail, saved.getId(), values);
        emailNotificationService.sendTemplate("VISITOR_CONFIRMATION", saved.getEmail(), saved.getId(), values);
        return Optional.of(new ContactResponse(saved.getId(), saved.getStatus(), saved.getCreatedAt()));
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }

    private String sanitizeSubject(String value) {
        return value == null ? "Professional enquiry" : value.replaceAll("[\\r\\n]+", " ");
    }
}
