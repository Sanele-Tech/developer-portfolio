package com.sanele.portfolio.achievements.controller;

import com.sanele.portfolio.achievements.dto.AchievementResponse;
import com.sanele.portfolio.achievements.dto.AchievementWriteRequest;
import com.sanele.portfolio.achievements.service.AchievementAdminService;
import com.sanele.portfolio.auth.security.PortfolioUserDetails;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/achievements")
@PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
public class AchievementAdminController {
    private final AchievementAdminService achievementAdminService;

    public AchievementAdminController(AchievementAdminService achievementAdminService) {
        this.achievementAdminService = achievementAdminService;
    }

    @GetMapping
    public List<AchievementResponse> getAchievements() {
        return achievementAdminService.getAchievements();
    }

    @GetMapping("/{id}")
    public AchievementResponse getAchievement(@PathVariable Long id) {
        return achievementAdminService.getAchievement(id);
    }

    @PostMapping
    public ResponseEntity<AchievementResponse> createAchievement(@Valid @RequestBody AchievementWriteRequest request,
            @AuthenticationPrincipal PortfolioUserDetails actor) {
        AchievementResponse achievement = achievementAdminService.createAchievement(request, actor);
        return ResponseEntity.created(URI.create("/api/admin/achievements/" + achievement.id()))
                .body(achievement);
    }

    @PutMapping("/{id}")
    public AchievementResponse updateAchievement(@PathVariable Long id,
            @Valid @RequestBody AchievementWriteRequest request,
            @AuthenticationPrincipal PortfolioUserDetails actor) {
        return achievementAdminService.updateAchievement(id, request, actor);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAchievement(@PathVariable Long id, @AuthenticationPrincipal PortfolioUserDetails actor) {
        achievementAdminService.deleteAchievement(id, actor);
    }
}
