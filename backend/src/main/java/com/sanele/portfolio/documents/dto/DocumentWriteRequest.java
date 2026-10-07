package com.sanele.portfolio.documents.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DocumentWriteRequest(
        @NotBlank @Size(max = 255) String name,
        @NotBlank @Size(max = 80) String documentType,
        @NotBlank @Size(max = 500)
        @Pattern(regexp = "https?://.+", message = "Must be an HTTP or HTTPS URL.")
        String storageUrl,
        @Size(max = 120) String mimeType,
        @Min(0) Long sizeBytes) {
}
