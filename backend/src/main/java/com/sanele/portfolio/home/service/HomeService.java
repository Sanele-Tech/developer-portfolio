package com.sanele.portfolio.home.service;

import com.sanele.portfolio.education.service.EducationService;
import com.sanele.portfolio.experience.service.ExperienceService;
import com.sanele.portfolio.home.dto.HomeResponse;
import com.sanele.portfolio.home.dto.QuickProfileResponse;
import com.sanele.portfolio.projects.service.ProjectService;
import com.sanele.portfolio.settings.repository.SiteSettingRepository;
import com.sanele.portfolio.skills.service.SkillService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class HomeService {
    private final ProjectService projectService;
    private final ExperienceService experienceService;
    private final EducationService educationService;
    private final SkillService skillService;
    private final SiteSettingRepository siteSettingRepository;

    public HomeService(ProjectService projectService, ExperienceService experienceService,
            EducationService educationService, SkillService skillService, SiteSettingRepository siteSettingRepository) {
        this.projectService = projectService;
        this.experienceService = experienceService;
        this.educationService = educationService;
        this.skillService = skillService;
        this.siteSettingRepository = siteSettingRepository;
    }

    public HomeResponse getHome() {
        List<String> focus = skillService.getSkills().stream()
                .map(skill -> skill.category())
                .filter(category -> category != null && !category.isBlank())
                .distinct()
                .toList();
        String education = educationService.getEducation().stream()
                .findFirst()
                .map(item -> item.qualification() + " at " + item.institution())
                .orElseGet(() -> setting("profile.education").orElse(null));
        QuickProfileResponse quickProfile = new QuickProfileResponse(
                education,
                setting("profile.location").orElse(null),
                setting("profile.availability").orElse(null),
                setting("profile.focus").map(value -> List.of(value.split(","))).orElse(focus));
        return new HomeResponse(
                quickProfile,
                projectService.getProjects().stream().filter(project -> project.featured()).limit(6).toList(),
                experienceService.getExperience().stream().limit(3).toList(),
                skillService.getSkills().stream().limit(12).toList());
    }

    private java.util.Optional<String> setting(String key) {
        return siteSettingRepository.findBySettingKey(key).map(setting -> setting.getSettingValue());
    }
}