package com.sanele.portfolio.skills.controller;

import com.sanele.portfolio.auth.security.PortfolioUserDetails;
import com.sanele.portfolio.skills.dto.SkillResponse;
import com.sanele.portfolio.skills.dto.SkillWriteRequest;
import com.sanele.portfolio.skills.service.SkillAdminService;
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
@RequestMapping("/api/admin/skills")
@PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
public class SkillAdminController {
    private final SkillAdminService skillAdminService;

    public SkillAdminController(SkillAdminService skillAdminService) {
        this.skillAdminService = skillAdminService;
    }

    @GetMapping
    public List<SkillResponse> getSkills() {
        return skillAdminService.getSkills();
    }

    @GetMapping("/{id}")
    public SkillResponse getSkill(@PathVariable Long id) {
        return skillAdminService.getSkill(id);
    }

    @PostMapping
    public ResponseEntity<SkillResponse> createSkill(@Valid @RequestBody SkillWriteRequest request,
            @AuthenticationPrincipal PortfolioUserDetails actor) {
        SkillResponse skill = skillAdminService.createSkill(request, actor);
        return ResponseEntity.created(URI.create("/api/admin/skills/" + skill.id())).body(skill);
    }

    @PutMapping("/{id}")
    public SkillResponse updateSkill(@PathVariable Long id, @Valid @RequestBody SkillWriteRequest request,
            @AuthenticationPrincipal PortfolioUserDetails actor) {
        return skillAdminService.updateSkill(id, request, actor);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSkill(@PathVariable Long id, @AuthenticationPrincipal PortfolioUserDetails actor) {
        skillAdminService.deleteSkill(id, actor);
    }
}
