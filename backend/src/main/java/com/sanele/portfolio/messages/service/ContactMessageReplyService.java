package com.sanele.portfolio.messages.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanele.portfolio.audit.entity.AuditLog;
import com.sanele.portfolio.audit.repository.AuditLogRepository;
import com.sanele.portfolio.auth.security.PortfolioUserDetails;
import com.sanele.portfolio.common.exception.ResourceNotFoundException;
import com.sanele.portfolio.email.service.EmailNotificationService;
import com.sanele.portfolio.messages.dto.ContactMessageReplyRecord;
import com.sanele.portfolio.messages.dto.ContactMessageReplyRequest;
import com.sanele.portfolio.messages.dto.ContactMessageReplyResponse;
import com.sanele.portfolio.messages.entity.ContactMessage;
import com.sanele.portfolio.messages.entity.ContactMessageReply;
import com.sanele.portfolio.messages.repository.ContactMessageReplyRepository;
import com.sanele.portfolio.messages.repository.ContactMessageRepository;
import com.sanele.portfolio.users.repository.UserRepository;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContactMessageReplyService {
    private final ContactMessageRepository messageRepository;
    private final ContactMessageReplyRepository replyRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final EmailNotificationService emailNotificationService;
    private final ObjectMapper objectMapper;

    public ContactMessageReplyService(ContactMessageRepository messageRepository,
            ContactMessageReplyRepository replyRepository, UserRepository userRepository,
            AuditLogRepository auditLogRepository, EmailNotificationService emailNotificationService,
            ObjectMapper objectMapper) {
        this.messageRepository = messageRepository;
        this.replyRepository = replyRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
        this.emailNotificationService = emailNotificationService;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<ContactMessageReplyRecord> getReplies(Long contactMessageId) {
        if (!messageRepository.existsById(contactMessageId)) {
            throw new ResourceNotFoundException("Contact message", contactMessageId.toString());
        }
        return replyRepository.findAllByContactMessageIdOrderByCreatedAtAsc(contactMessageId)
                .stream()
                .map(reply -> new ContactMessageReplyRecord(reply.getId(),
                        reply.getAuthor() == null ? "Former user" : reply.getAuthor().getDisplayName(),
                        reply.getBody(), reply.getCreatedAt()))
                .toList();
    }

    @Transactional
    public ContactMessageReplyResponse reply(Long contactMessageId, ContactMessageReplyRequest request,
            PortfolioUserDetails actor) {
        ContactMessage contactMessage = messageRepository.findById(contactMessageId)
                .orElseThrow(() -> new ResourceNotFoundException("Contact message", contactMessageId.toString()));
        String body = request.message().trim();
        ContactMessageReply reply = replyRepository.save(new ContactMessageReply(contactMessage,
                userRepository.getReferenceById(actor.getId()), body));
        Map<String, String> values = Map.of(
                "name", contactMessage.getName(),
                "email", contactMessage.getEmail(),
                "company", contactMessage.getCompany() == null ? "Not provided" : contactMessage.getCompany(),
                "subject", sanitizeSubject(contactMessage.getSubject()),
                "message", body);
        String templateName = request.templateName() == null ? "RECRUITER_RESPONSE" : request.templateName();
        boolean delivered = emailNotificationService.sendTemplate(templateName,
                contactMessage.getEmail(), contactMessage.getId(), values);
        if (delivered) {
            contactMessage.updateStatus(ContactMessage.Status.REPLIED);
            messageRepository.save(contactMessage);
        }
        auditLogRepository.save(new AuditLog(userRepository.getReferenceById(actor.getId()),
                "REPLY_" + (delivered ? "SENT" : "DELIVERY_FAILED"), "CONTACT_MESSAGE",
                contactMessage.getId(), objectMapper.valueToTree(Map.of("emailDelivered", delivered))));
        return new ContactMessageReplyResponse(reply.getId(), actor.getDisplayName(), body,
                reply.getCreatedAt(), delivered);
    }

    private String sanitizeSubject(String subject) {
        return subject == null ? "Professional enquiry" : subject.replaceAll("[\\r\\n]+", " ");
    }
}
