package com.sanele.portfolio.experience.controller;

import com.sanele.portfolio.experience.dto.ExperienceResponse;
import com.sanele.portfolio.experience.service.ExperienceService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/experience")
public class ExperienceController {
    private final ExperienceService experienceService;
    public ExperienceController(ExperienceService experienceService) { this.experienceService = experienceService; }
    @GetMapping
    public List<ExperienceResponse> getExperience() { return experienceService.getExperience(); }
}