package com.sanele.portfolio.experience.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record ExperienceWriteRequest(
        @NotBlank @Size(max = 160) String jobTitle,
        @NotBlank @Size(max = 160) String company,
        @NotBlank String description,
        @NotNull LocalDate startDate,
        LocalDate endDate,
        boolean currentRole,
        @Min(0) @Max(100000) int sortOrder) {

    @AssertTrue(message = "End date must not precede start date, and current roles must not have an end date.")
    public boolean isDateRangeValid() {
        return startDate == null
                || ((!currentRole || endDate == null) && (endDate == null || !endDate.isBefore(startDate)));
    }
}
