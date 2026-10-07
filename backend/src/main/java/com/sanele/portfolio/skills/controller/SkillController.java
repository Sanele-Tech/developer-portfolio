package com.sanele.portfolio.skills.controller;

import com.sanele.portfolio.skills.dto.SkillResponse;
import com.sanele.portfolio.skills.service.SkillService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/skills")
public class SkillController {
    private final SkillService skillService;
    public SkillController(SkillService skillService) { this.skillService = skillService; }
    @GetMapping
    public List<SkillResponse> getSkills() { return skillService.getSkills(); }
}