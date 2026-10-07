package com.sanele.portfolio.admin.dto;

public record AdminOverviewResponse(
        long projects,
        long experience,
        long education,
        long skills,
        long achievements,
        long documents,
        long publishedPages,
        long draftPages,
        long newMessages) {
}
