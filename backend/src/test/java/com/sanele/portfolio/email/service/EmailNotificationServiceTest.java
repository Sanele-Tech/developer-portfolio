package com.sanele.portfolio.email.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

import com.sanele.portfolio.email.dto.EmailDeliveryStatus;
import com.sanele.portfolio.email.entity.EmailTemplate;
import com.sanele.portfolio.email.repository.EmailTemplateRepository;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;

@ExtendWith(MockitoExtension.class)
class EmailNotificationServiceTest {
    @Mock private EmailTemplateRepository templateRepository;
    @Mock private EmailDeliveryLogService deliveryLogService;
    @Mock private EmailTransport emailTransport;

    @Test
    void rendersAndSendsPlainTextTemplate() {
        when(templateRepository.findByName("VISITOR_CONFIRMATION"))
                .thenReturn(Optional.of(new EmailTemplate("VISITOR_CONFIRMATION", "Hello {{name}}",
                        "Thanks, {{name}}. {{message}}")));
        EmailNotificationService service = new EmailNotificationService(
                templateRepository, deliveryLogService, emailTransport);

        boolean sent = service.sendTemplate("VISITOR_CONFIRMATION", "visitor@example.test", 9L,
                Map.of("name", "Taylor", "message", "I received your message."));

        assertThat(sent).isTrue();
        verify(emailTransport).send("visitor@example.test", "Hello Taylor",
                "Thanks, Taylor. I received your message.");
        verify(deliveryLogService).record(9L, "VISITOR_CONFIRMATION", "visitor@example.test",
                EmailDeliveryStatus.SENT, null);
    }

    @Test
    void recordsSanitizedFailureWhenSmtpDeliveryFails() {
        when(templateRepository.findByName("VISITOR_CONFIRMATION"))
                .thenReturn(Optional.of(new EmailTemplate("VISITOR_CONFIRMATION", "Hello", "Body")));
        org.mockito.Mockito.doThrow(new MailSendException("sensitive provider details"))
                .when(emailTransport).send("visitor@example.test", "Hello", "Body");
        EmailNotificationService service = new EmailNotificationService(
                templateRepository, deliveryLogService, emailTransport);

        boolean sent = service.sendTemplate("VISITOR_CONFIRMATION", "visitor@example.test", 9L, Map.of());

        assertThat(sent).isFalse();
        ArgumentCaptor<String> reason = ArgumentCaptor.forClass(String.class);
        verify(deliveryLogService).record(org.mockito.ArgumentMatchers.eq(9L),
                org.mockito.ArgumentMatchers.eq("VISITOR_CONFIRMATION"),
                org.mockito.ArgumentMatchers.eq("visitor@example.test"),
                org.mockito.ArgumentMatchers.eq(EmailDeliveryStatus.FAILED), reason.capture());
        assertThat(reason.getValue()).doesNotContain("sensitive provider details");
        verify(deliveryLogService, never()).record(any(), any(), any(), org.mockito.ArgumentMatchers.eq(EmailDeliveryStatus.SENT), any());
    }
}
