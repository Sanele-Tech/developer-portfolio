package com.sanele.portfolio.messages.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanele.portfolio.audit.entity.AuditLog;
import com.sanele.portfolio.audit.repository.AuditLogRepository;
import com.sanele.portfolio.auth.security.PortfolioUserDetails;
import com.sanele.portfolio.common.exception.ResourceNotFoundException;
import com.sanele.portfolio.messages.dto.ContactMessageResponse;
import com.sanele.portfolio.messages.dto.ContactMessageStatusRequest;
import com.sanele.portfolio.messages.entity.ContactMessage;
import com.sanele.portfolio.messages.repository.ContactMessageRepository;
import com.sanele.portfolio.users.repository.UserRepository;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ContactMessageAdminService {
    private final ContactMessageRepository messageRepository;
    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public ContactMessageAdminService(ContactMessageRepository messageRepository,
            AuditLogRepository auditLogRepository, UserRepository userRepository, ObjectMapper objectMapper) {
        this.messageRepository = messageRepository;
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    public Page<ContactMessageResponse> getMessages(int page, int size) {
        return messageRepository.findAllByOrderByCreatedAtDescIdDesc(PageRequest.of(page, size))
                .map(this::toResponse);
    }

    @Transactional
    public ContactMessageResponse updateStatus(Long id, ContactMessageStatusRequest request,
            PortfolioUserDetails actor) {
        ContactMessage message = messageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact message", id.toString()));
        ContactMessage.Status previousStatus = message.getStatus();
        if (previousStatus == request.status()) {
            return toResponse(message);
        }
        message.updateStatus(request.status());
        messageRepository.save(message);

        Map<String, Object> details = Map.of(
                "before", Map.of("status", previousStatus.name()),
                "after", Map.of("status", request.status().name()));
        auditLogRepository.save(new AuditLog(userRepository.getReferenceById(actor.getId()),
                "UPDATE_STATUS", "CONTACT_MESSAGE", message.getId(), objectMapper.valueToTree(details)));
        return toResponse(message);
    }

    private ContactMessageResponse toResponse(ContactMessage message) {
        return new ContactMessageResponse(message.getId(), message.getName(), message.getEmail(),
                message.getSubject(), message.getCompany(), message.getMessage(), message.getStatus(),
                message.getCreatedAt());
    }
}
