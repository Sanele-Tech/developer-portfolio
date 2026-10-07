package com.sanele.portfolio.projects.service;

import com.sanele.portfolio.common.exception.ResourceNotFoundException;
import com.sanele.portfolio.projects.dto.ProjectImageResponse;
import com.sanele.portfolio.projects.dto.ProjectLinkResponse;
import com.sanele.portfolio.projects.dto.ProjectResponse;
import com.sanele.portfolio.projects.dto.TechnologyResponse;
import com.sanele.portfolio.projects.entity.Project;
import com.sanele.portfolio.projects.repository.ProjectImageRepository;
import com.sanele.portfolio.projects.repository.ProjectLinkRepository;
import com.sanele.portfolio.projects.repository.ProjectRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectLinkRepository projectLinkRepository;
    private final ProjectImageRepository projectImageRepository;

    public ProjectService(
            ProjectRepository projectRepository,
            ProjectLinkRepository projectLinkRepository,
            ProjectImageRepository projectImageRepository) {
        this.projectRepository = projectRepository;
        this.projectLinkRepository = projectLinkRepository;
        this.projectImageRepository = projectImageRepository;
    }

    public List<ProjectResponse> getProjects() {
        return projectRepository.findAllByOrderBySortOrderAscIdAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    public Optional<ProjectResponse> getProject(String slug) {
        return Optional.of(projectRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Project", slug)))
                .map(this::toResponse);
    }

    private ProjectResponse toResponse(Project project) {
        List<TechnologyResponse> technologyDetails = project.getTechnologyEntities().stream()
                .map(technology -> new TechnologyResponse(technology.getName(), technology.getSlug()))
                .sorted((left, right) -> left.name().toLowerCase(Locale.ROOT)
                        .compareTo(right.name().toLowerCase(Locale.ROOT)))
                .toList();
        List<String> technologies = technologyDetails.isEmpty()
                ? Arrays.stream(project.getTechnologies().split(","))
                        .map(String::trim)
                        .filter(value -> !value.isBlank())
                        .toList()
                : technologyDetails.stream().map(TechnologyResponse::name).toList();

        List<ProjectLinkResponse> links = projectLinkRepository.findByProjectId(project.getId()).stream()
                .map(link -> new ProjectLinkResponse(link.getLabel(), link.getUrl(), link.getLinkType()))
                .toList();
        List<ProjectImageResponse> images = projectImageRepository
                .findByProjectIdOrderBySortOrderAsc(project.getId()).stream()
                .map(image -> new ProjectImageResponse(image.getImageUrl(), image.getAltText(), image.getHeading(),
                        image.getDescription(), image.getSortOrder()))
                .toList();

        return new ProjectResponse(
                project.getId(), project.getTitle(), project.getSlug(), project.getDescription(), technologies,
                technologyDetails, project.getRepositoryUrl(), project.getLiveUrl(), project.getImageUrl(),
                project.isFeatured(), project.getSortOrder(), links, images, project.getCreatedAt(), project.getUpdatedAt());
    }
}