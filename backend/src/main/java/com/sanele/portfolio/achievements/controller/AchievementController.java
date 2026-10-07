package com.sanele.portfolio.achievements.controller;

import com.sanele.portfolio.achievements.dto.AchievementResponse;
import com.sanele.portfolio.achievements.service.AchievementService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/achievements")
public class AchievementController {
    private final AchievementService achievementService;
    public AchievementController(AchievementService achievementService) { this.achievementService = achievementService; }
    @GetMapping
    public List<AchievementResponse> getAchievements() { return achievementService.getAchievements(); }
}