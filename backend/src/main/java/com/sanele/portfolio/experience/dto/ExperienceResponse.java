package com.sanele.portfolio.experience.dto;

import java.time.LocalDate;

public record ExperienceResponse(Long id, String jobTitle, String company, String description,
        LocalDate startDate, LocalDate endDate, boolean currentRole, int sortOrder) { }