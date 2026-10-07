package com.sanele.portfolio.email.service;

public interface EmailTransport {
    void send(String recipient, String subject, String body);
}
