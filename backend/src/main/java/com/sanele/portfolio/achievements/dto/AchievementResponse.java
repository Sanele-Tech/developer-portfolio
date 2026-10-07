package com.sanele.portfolio.achievements.dto;

import java.time.LocalDate;
import java.util.List;

public record AchievementResponse(Long id, String title, String cardType, String issuer, String description,
        LocalDate achievedOn, String url, String imageUrl, int sortOrder, List<String> imageUrls) { }