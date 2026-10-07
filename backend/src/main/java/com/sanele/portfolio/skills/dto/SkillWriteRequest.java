package com.sanele.portfolio.skills.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SkillWriteRequest(
        @NotBlank @Size(max = 120) String name,
        @Size(max = 120) String category,
        @Min(0) @Max(100) Integer proficiency,
        @Min(0) @Max(100000) int sortOrder) {
}
