package com.sanele.portfolio.projects.dto;

import java.time.Instant;
import java.util.List;

public record ProjectResponse(
        Long id,
        String title,
        String slug,
        String description,
        List<String> technologies,
        List<TechnologyResponse> technologyDetails,
        String repositoryUrl,
        String liveUrl,
        String imageUrl,
        boolean featured,
        int sortOrder,
        List<ProjectLinkResponse> links,
        List<ProjectImageResponse> images,
        Instant createdAt,
        Instant updatedAt) { }