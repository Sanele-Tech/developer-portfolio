package com.sanele.portfolio.projects.controller;

import com.sanele.portfolio.projects.dto.ProjectResponse;
import com.sanele.portfolio.projects.service.ProjectService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    private final ProjectService projectService;
    public ProjectController(ProjectService projectService) { this.projectService = projectService; }
    @GetMapping
    public List<ProjectResponse> getProjects() { return projectService.getProjects(); }
    @GetMapping("/{slug}")
    public ResponseEntity<ProjectResponse> getProject(@PathVariable String slug) {
        return projectService.getProject(slug).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}