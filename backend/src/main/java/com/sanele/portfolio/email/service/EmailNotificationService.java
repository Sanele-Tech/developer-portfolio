package com.sanele.portfolio.email.service;

import com.sanele.portfolio.email.dto.EmailDeliveryStatus;
import com.sanele.portfolio.email.entity.EmailTemplate;
import com.sanele.portfolio.email.repository.EmailTemplateRepository;
import java.util.Map;
import java.util.regex.Pattern;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService {
    private static final Pattern UNRESOLVED_TOKEN = Pattern.compile("\\{\\{[^{}]+}}");
    private final EmailTemplateRepository templateRepository;
    private final EmailDeliveryLogService deliveryLogService;
    private final EmailTransport emailTransport;

    public EmailNotificationService(EmailTemplateRepository templateRepository,
            EmailDeliveryLogService deliveryLogService, EmailTransport emailTransport) {
        this.templateRepository = templateRepository;
        this.deliveryLogService = deliveryLogService;
        this.emailTransport = emailTransport;
    }

    public boolean sendTemplate(String templateName, String recipient, Long contactMessageId,
            Map<String, String> values) {
        try {
            if (recipient == null || recipient.isBlank()) {
                throw new EmailTransportUnavailableException(
                        "The notification recipient is not configured. Set PORTFOLIO_NOTIFICATION_EMAIL.");
            }
            EmailTemplate template = templateRepository.findByName(templateName)
                    .orElseThrow(() -> new EmailTemplateUnavailableException(templateName));
            String subject = render(template.getSubject(), values);
            String body = render(template.getBody(), values);
            if (subject.contains("\r") || subject.contains("\n")) {
                throw new EmailTemplateUnavailableException(templateName);
            }
            emailTransport.send(recipient, subject, body);
            deliveryLogService.record(contactMessageId, templateName, recipient,
                    EmailDeliveryStatus.SENT, null);
            return true;
        } catch (MailException | EmailTransportUnavailableException | EmailTemplateUnavailableException exception) {
            String reason = exception instanceof EmailTransportUnavailableException
                    ? exception.getMessage()
                    : "Email delivery failed (" + exception.getClass().getSimpleName()
                            + "). Check server-side SMTP settings and provider logs.";
            deliveryLogService.record(contactMessageId, templateName, recipient,
                    EmailDeliveryStatus.FAILED, reason);
            return false;
        }
    }

    private String render(String template, Map<String, String> values) {
        String rendered = template;
        for (Map.Entry<String, String> value : values.entrySet()) {
            rendered = rendered.replace("{{" + value.getKey() + "}}",
                    value.getValue() == null ? "" : value.getValue());
        }
        if (UNRESOLVED_TOKEN.matcher(rendered).find()) {
            throw new EmailTemplateUnavailableException("unresolved placeholder");
        }
        return rendered;
    }
}
