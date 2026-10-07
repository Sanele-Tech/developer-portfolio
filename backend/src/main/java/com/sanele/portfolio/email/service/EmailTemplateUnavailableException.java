package com.sanele.portfolio.email.service;

public class EmailTemplateUnavailableException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public EmailTemplateUnavailableException(String templateName) {
        super("The email template is missing or has invalid placeholders: " + templateName);
    }
}
