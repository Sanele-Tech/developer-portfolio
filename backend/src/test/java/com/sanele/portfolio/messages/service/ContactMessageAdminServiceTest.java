package com.sanele.portfolio.messages.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanele.portfolio.audit.entity.AuditLog;
import com.sanele.portfolio.audit.repository.AuditLogRepository;
import com.sanele.portfolio.auth.security.PortfolioUserDetails;
import com.sanele.portfolio.common.exception.ResourceNotFoundException;
import com.sanele.portfolio.messages.dto.ContactMessageStatusRequest;
import com.sanele.portfolio.messages.entity.ContactMessage;
import com.sanele.portfolio.messages.repository.ContactMessageRepository;
import com.sanele.portfolio.users.entity.User;
import com.sanele.portfolio.users.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class ContactMessageAdminServiceTest {
    private static final PortfolioUserDetails ACTOR =
            new PortfolioUserDetails(5L, "admin", "admin@example.test", "Admin", "", true, List.of());
    private final ContactMessageRepository messageRepository = mock(ContactMessageRepository.class);
    private final AuditLogRepository auditLogRepository = mock(AuditLogRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final ContactMessageAdminService service = new ContactMessageAdminService(messageRepository,
            auditLogRepository, userRepository, new ObjectMapper().findAndRegisterModules());

    @Test
    void getMessagesReturnsPagedContactMessages() {
        ContactMessage message = new ContactMessage("Recruiter", "recruiter@example.test",
                "Opportunity", "Let's talk");
        when(messageRepository.findAllByOrderByCreatedAtDescIdDesc(PageRequest.of(0, 20)))
                .thenReturn(new PageImpl<>(List.of(message)));

        var result = service.getMessages(0, 20);

        assertEquals(1, result.getTotalElements());
        assertEquals("Recruiter", result.getContent().getFirst().name());
        assertEquals("Let's talk", result.getContent().getFirst().message());
    }

    @Test
    void statusChangeIsAudited() {
        ContactMessage message = new ContactMessage("Recruiter", "recruiter@example.test",
                "Opportunity", "Let's talk");
        when(messageRepository.findById(9L)).thenReturn(Optional.of(message));
        when(messageRepository.save(message)).thenReturn(message);
        when(userRepository.getReferenceById(ACTOR.getId())).thenReturn(mock(User.class));

        var response = service.updateStatus(9L,
                new ContactMessageStatusRequest(ContactMessage.Status.READ), ACTOR);

        assertEquals(ContactMessage.Status.READ, response.status());
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        assertEquals("UPDATE_STATUS", captor.getValue().getAction());
        assertEquals("CONTACT_MESSAGE", captor.getValue().getEntityType());
        assertEquals("NEW", captor.getValue().getDetails().path("before").path("status").asText());
        assertEquals("READ", captor.getValue().getDetails().path("after").path("status").asText());
    }

    @Test
    void statusChangeRejectsMissingMessage() {
        when(messageRepository.findById(9L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.updateStatus(9L,
                new ContactMessageStatusRequest(ContactMessage.Status.READ), ACTOR));
    }
}
