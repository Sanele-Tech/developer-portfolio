package com.sanele.portfolio.education.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public record EducationWriteRequest(
        @NotBlank @Size(max = 200) String institution,
        @NotBlank @Size(max = 200) String qualification,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        @Min(0) @Max(100000) int sortOrder,
        @Size(max = 30) List<@NotBlank @Size(max = 120) String> coreModules,
        @Size(max = 10000) String experience,
        @Size(max = 10) List<@Size(max = 500) String> imageUrls) {

    @AssertTrue(message = "End date must not precede start date.")
    public boolean isDateRangeValid() {
        return startDate == null || endDate == null || !endDate.isBefore(startDate);
    }
}
