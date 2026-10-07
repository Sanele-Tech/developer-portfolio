package com.sanele.portfolio.messages.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContactRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Email @Size(max = 320) String email,
        @Size(max = 160) String company,
        @Size(max = 200) String subject,
        @NotBlank @Size(max = 5000) String message,
        @Size(max = 200) String website) {
    public ContactRequest(String name, String email, String subject, String message) {
        this(name, email, null, subject, message, null);
    }
}