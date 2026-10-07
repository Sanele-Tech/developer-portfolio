package com.sanele.portfolio.messages.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ContactMessageReplyRequest(
        @NotBlank @Size(max = 5000) String message,
        @Pattern(regexp = "RECRUITER_RESPONSE|APPLICATION_FOLLOW_UP|PROFESSIONAL_ENQUIRY")
        String templateName) {
    public ContactMessageReplyRequest(String message) {
        this(message, "RECRUITER_RESPONSE");
    }
}
