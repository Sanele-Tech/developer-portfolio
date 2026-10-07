package com.sanele.portfolio.content.dto;

import com.sanele.portfolio.content.entity.Page.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PageWriteRequest(
        @NotBlank @Size(max = 180)
        @Pattern(regexp = "[a-z0-9]+(?:-[a-z0-9]+)*", message = "Use lowercase letters, numbers, and hyphens.")
        String slug,
        @NotBlank @Size(max = 200) String title,
        String excerpt,
        @NotBlank String content,
        @NotNull Status status) {
}
