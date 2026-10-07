package com.sanele.portfolio.messages.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sanele.portfolio.email.service.EmailNotificationService;
import com.sanele.portfolio.messages.dto.ContactRequest;
import com.sanele.portfolio.messages.entity.ContactMessage;
import com.sanele.portfolio.messages.repository.ContactMessageRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ContactMessageServiceTest {
    @Mock private ContactMessageRepository repository;
    @Mock private EmailNotificationService emailNotificationService;

    @Test
    void storesEnquiryAndQueuesAdminAndVisitorEmailNotifications() {
        ContactMessageService service = new ContactMessageService(repository, emailNotificationService,
                "owner@example.test");
        ContactMessage message = new ContactMessage("Recruiter", "recruiter@example.test", "Example Ltd",
                "Role", "Please contact me.");
        when(repository.save(any(ContactMessage.class))).thenReturn(message);

        Optional<?> response = service.submit(new ContactRequest(" Recruiter ", " recruiter@example.test ",
                "Example Ltd", "Role", " Please contact me. ", ""));

        assertThat(response).isPresent();
        ArgumentCaptor<ContactMessage> messageCaptor = ArgumentCaptor.forClass(ContactMessage.class);
        verify(repository).save(messageCaptor.capture());
        assertThat(messageCaptor.getValue().getName()).isEqualTo("Recruiter");
        assertThat(messageCaptor.getValue().getCompany()).isEqualTo("Example Ltd");
        verify(emailNotificationService).sendTemplate(eq("CONTACT_NOTIFICATION"),
                eq("owner@example.test"), eq(null), any());
        verify(emailNotificationService).sendTemplate(eq("VISITOR_CONFIRMATION"),
                eq("recruiter@example.test"), eq(null), any());
    }

    @Test
    void honeypotSubmissionIsSilentlyDiscarded() {
        ContactMessageService service = new ContactMessageService(repository, emailNotificationService,
                "owner@example.test");

        assertThat(service.submit(new ContactRequest("Bot", "bot@example.test", null, null,
                "Message", "http://spam.example"))).isEmpty();
        verify(repository, never()).save(any());
        verify(emailNotificationService, never()).sendTemplate(any(), any(), any(), any());
    }
}
