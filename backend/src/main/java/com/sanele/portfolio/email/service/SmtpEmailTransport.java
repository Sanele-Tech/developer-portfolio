package com.sanele.portfolio.email.service;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class SmtpEmailTransport implements EmailTransport {
    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final String smtpHost;
    private final String fromAddress;

    public SmtpEmailTransport(ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${spring.mail.host:}") String smtpHost,
            @Value("${portfolio.mail.from:${spring.mail.username:}}") String fromAddress) {
        this.mailSenderProvider = mailSenderProvider;
        this.smtpHost = smtpHost;
        this.fromAddress = fromAddress;
    }

    @Override
    public void send(String recipient, String subject, String body) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (smtpHost.isBlank() || fromAddress.isBlank() || mailSender == null) {
            throw new EmailTransportUnavailableException(
                    "SMTP is not configured. Set SPRING_MAIL_HOST and PORTFOLIO_MAIL_FROM (or SPRING_MAIL_USERNAME).");
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(recipient);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
}
