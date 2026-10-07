package com.sanele.portfolio.email.service;

public class EmailTransportUnavailableException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public EmailTransportUnavailableException(String message) {
        super(message);
    }
}
