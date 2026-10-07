package com.sanele.portfolio.email.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EmailTemplateRequest(
        @NotBlank @Size(max = 120) @Pattern(regexp = "^[A-Z][A-Z0-9_]*$") String name,
        @NotBlank @Size(max = 200) @Pattern(regexp = "^[^\\r\\n]*$") String subject,
        @NotBlank @Size(max = 20000) String body) {
}
