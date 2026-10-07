package com.sanele.portfolio.home.dto;

import com.sanele.portfolio.experience.dto.ExperienceResponse;
import com.sanele.portfolio.projects.dto.ProjectResponse;
import com.sanele.portfolio.skills.dto.SkillResponse;
import java.util.List;

public record HomeResponse(QuickProfileResponse quickProfile, List<ProjectResponse> featuredProjects,
        List<ExperienceResponse> experienceSummary, List<SkillResponse> skillsSummary) { }