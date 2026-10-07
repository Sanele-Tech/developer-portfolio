package com.sanele.portfolio.experience.controller;

import com.sanele.portfolio.auth.security.PortfolioUserDetails;
import com.sanele.portfolio.experience.dto.ExperienceResponse;
import com.sanele.portfolio.experience.dto.ExperienceWriteRequest;
import com.sanele.portfolio.experience.service.ExperienceAdminService;
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
@RequestMapping("/api/admin/experience")
@PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
public class ExperienceAdminController {
    private final ExperienceAdminService experienceAdminService;

    public ExperienceAdminController(ExperienceAdminService experienceAdminService) {
        this.experienceAdminService = experienceAdminService;
    }

    @GetMapping
    public List<ExperienceResponse> getExperience() {
        return experienceAdminService.getExperience();
    }

    @GetMapping("/{id}")
    public ExperienceResponse getExperience(@PathVariable Long id) {
        return experienceAdminService.getExperience(id);
    }

    @PostMapping
    public ResponseEntity<ExperienceResponse> createExperience(@Valid @RequestBody ExperienceWriteRequest request,
            @AuthenticationPrincipal PortfolioUserDetails actor) {
        ExperienceResponse experience = experienceAdminService.createExperience(request, actor);
        return ResponseEntity.created(URI.create("/api/admin/experience/" + experience.id())).body(experience);
    }

    @PutMapping("/{id}")
    public ExperienceResponse updateExperience(@PathVariable Long id,
            @Valid @RequestBody ExperienceWriteRequest request,
            @AuthenticationPrincipal PortfolioUserDetails actor) {
        return experienceAdminService.updateExperience(id, request, actor);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteExperience(@PathVariable Long id, @AuthenticationPrincipal PortfolioUserDetails actor) {
        experienceAdminService.deleteExperience(id, actor);
    }
}
