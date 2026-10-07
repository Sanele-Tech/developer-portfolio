package com.sanele.portfolio.messages.dto;

public class ContactRateLimitExceededException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ContactRateLimitExceededException() {
        super("Too many contact requests. Please try again later.");
    }
}
