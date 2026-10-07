package com.sanele.portfolio.achievements.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public record AchievementWriteRequest(
        @NotBlank @Size(max = 200) String title,
        @Pattern(regexp = "CERTIFICATION|BUSINESS|COMMUNITY|CHALLENGE|OTHER")
        String cardType,
        @Size(max = 160) String issuer,
        String description,
        LocalDate achievedOn,
        @Size(max = 500) String url,
        @Size(max = 500) String imageUrl,
        @Min(0) @Max(100000) int sortOrder,
        @Size(max = 10) List<@Size(max = 500) String> imageUrls) {
}
