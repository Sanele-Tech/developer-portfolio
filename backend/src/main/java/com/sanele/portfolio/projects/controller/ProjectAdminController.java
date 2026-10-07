package com.sanele.portfolio.projects.controller;

import com.sanele.portfolio.auth.security.PortfolioUserDetails;
import com.sanele.portfolio.projects.dto.ProjectResponse;
import com.sanele.portfolio.projects.dto.ProjectWriteRequest;
import com.sanele.portfolio.projects.dto.TechnologyAdminResponse;
import com.sanele.portfolio.projects.service.ProjectAdminService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.net.URI;
import java.util.List;
import org.springframework.data.domain.Page;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
public class ProjectAdminController {
    private final ProjectAdminService projectAdminService;

    public ProjectAdminController(ProjectAdminService projectAdminService) {
        this.projectAdminService = projectAdminService;
    }

    @GetMapping("/projects")
    public Page<ProjectResponse> getProjects(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return projectAdminService.getProjects(page, size);
    }

    @GetMapping("/projects/{id}")
    public ProjectResponse getProject(@PathVariable Long id) {
        return projectAdminService.getProject(id);
    }

    @PostMapping("/projects")
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody ProjectWriteRequest request,
            @AuthenticationPrincipal PortfolioUserDetails actor) {
        ProjectResponse project = projectAdminService.createProject(request, actor);
        return ResponseEntity.created(URI.create("/api/admin/projects/" + project.id())).body(project);
    }

    @PutMapping("/projects/{id}")
    public ProjectResponse updateProject(@PathVariable Long id, @Valid @RequestBody ProjectWriteRequest request,
            @AuthenticationPrincipal PortfolioUserDetails actor) {
        return projectAdminService.updateProject(id, request, actor);
    }

    @DeleteMapping("/projects/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProject(@PathVariable Long id, @AuthenticationPrincipal PortfolioUserDetails actor) {
        projectAdminService.deleteProject(id, actor);
    }

    @GetMapping("/technologies")
    public List<TechnologyAdminResponse> getTechnologies() {
        return projectAdminService.getTechnologies();
    }
}
