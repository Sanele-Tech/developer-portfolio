package com.sanele.portfolio.messages.controller;

import com.sanele.portfolio.messages.dto.ContactRequest;
import com.sanele.portfolio.messages.service.ContactMessageService;
import com.sanele.portfolio.messages.service.ContactRateLimiter;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contact")
public class ContactController {
    private final ContactMessageService contactMessageService;
    private final ContactRateLimiter rateLimiter;

    public ContactController(ContactMessageService contactMessageService, ContactRateLimiter rateLimiter) {
        this.contactMessageService = contactMessageService;
        this.rateLimiter = rateLimiter;
    }

    @PostMapping
    public ResponseEntity<?> createContactMessage(@Valid @RequestBody ContactRequest request,
            HttpServletRequest servletRequest) {
        rateLimiter.check(servletRequest.getRemoteAddr());
        return contactMessageService.submit(request)
                .map(response -> ResponseEntity.status(201).body(response))
                .orElseGet(() -> ResponseEntity.accepted().build());
    }
}