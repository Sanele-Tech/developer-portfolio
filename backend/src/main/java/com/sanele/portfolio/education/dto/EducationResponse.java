package com.sanele.portfolio.education.dto;

import java.time.LocalDate;
import java.util.List;

public record EducationResponse(Long id, String institution, String qualification, String description,
        LocalDate startDate, LocalDate endDate, int sortOrder, List<String> coreModules,
        String experience, List<String> imageUrls) { }