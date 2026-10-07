package com.sanele.portfolio.projects.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanele.portfolio.auth.security.PortfolioUserDetails;
import com.sanele.portfolio.audit.entity.AuditLog;
import com.sanele.portfolio.audit.repository.AuditLogRepository;
import com.sanele.portfolio.common.exception.ConflictException;
import com.sanele.portfolio.common.exception.ResourceNotFoundException;
import com.sanele.portfolio.projects.dto.ProjectImageResponse;
import com.sanele.portfolio.projects.dto.ProjectLinkResponse;
import com.sanele.portfolio.projects.dto.ProjectResponse;
import com.sanele.portfolio.projects.dto.ProjectWriteRequest;
import com.sanele.portfolio.projects.dto.TechnologyAdminResponse;
import com.sanele.portfolio.projects.dto.TechnologyResponse;
import com.sanele.portfolio.projects.entity.Project;
import com.sanele.portfolio.projects.entity.ProjectImage;
import com.sanele.portfolio.projects.entity.ProjectLink;
import com.sanele.portfolio.projects.entity.Technology;
import com.sanele.portfolio.projects.repository.ProjectImageRepository;
import com.sanele.portfolio.projects.repository.ProjectLinkRepository;
import com.sanele.portfolio.projects.repository.ProjectRepository;
import com.sanele.portfolio.projects.repository.TechnologyRepository;
import com.sanele.portfolio.users.repository.UserRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectAdminService {
    private final ProjectRepository projectRepository;
    private final ProjectLinkRepository projectLinkRepository;
    private final ProjectImageRepository projectImageRepository;
    private final TechnologyRepository technologyRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final ProjectService projectService;
    private final ObjectMapper objectMapper;

    public ProjectAdminService(ProjectRepository projectRepository, ProjectLinkRepository projectLinkRepository,
            ProjectImageRepository projectImageRepository, TechnologyRepository technologyRepository,
            UserRepository userRepository, AuditLogRepository auditLogRepository, ProjectService projectService,
            ObjectMapper objectMapper) {
        this.projectRepository = projectRepository;
        this.projectLinkRepository = projectLinkRepository;
        this.projectImageRepository = projectImageRepository;
        this.technologyRepository = technologyRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
        this.projectService = projectService;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public Page<ProjectResponse> getProjects(int page, int size) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Order.asc("sortOrder"), Sort.Order.asc("id")));
        return projectRepository.findAll(pageable).map(project -> getProjectResponse(project.getSlug()));
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", id.toString()));
        return getProjectResponse(project.getSlug());
    }

    @Transactional(readOnly = true)
    public List<TechnologyAdminResponse> getTechnologies() {
        return technologyRepository.findAllByOrderByNameAsc().stream()
                .map(technology -> new TechnologyAdminResponse(
                        technology.getId(), technology.getName(), technology.getSlug()))
                .toList();
    }

    @Transactional
    public ProjectResponse createProject(ProjectWriteRequest request, PortfolioUserDetails actor) {
        if (projectRepository.existsBySlug(request.slug())) {
            throw new ConflictException("A project with this slug already exists.");
        }

        Project project = new Project();
        applyRequest(project, request);
        projectRepository.save(project);
        saveChildren(project, request);
        writeAudit(actor, "CREATE", project, null, snapshot(project));
        return getProjectResponse(project.getSlug());
    }

    @Transactional
    public ProjectResponse updateProject(Long id, ProjectWriteRequest request, PortfolioUserDetails actor) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", id.toString()));
        Map<String, String> before = snapshot(project);
        if (projectRepository.existsBySlugAndIdNot(request.slug(), id)) {
            throw new ConflictException("A project with this slug already exists.");
        }

        applyRequest(project, request);
        projectRepository.save(project);
        projectLinkRepository.deleteByProjectId(id);
        projectImageRepository.deleteByProjectId(id);
        saveChildren(project, request);
        writeAudit(actor, "UPDATE", project, before, snapshot(project));
        return getProjectResponse(project.getSlug());
    }

    @Transactional
    public void deleteProject(Long id, PortfolioUserDetails actor) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", id.toString()));
        writeAudit(actor, "DELETE", project, snapshot(project), null);
        projectRepository.delete(project);
    }

    private void applyRequest(Project project, ProjectWriteRequest request) {
        List<Technology> technologies = request.technologyNames() != null
                ? resolveTechnologiesByName(request.technologyNames())
                : resolveTechnologiesById(request.technologyIds());
        technologies.sort((left, right) -> left.getName().compareToIgnoreCase(right.getName()));
        project.updateContent(request.title().trim(), request.slug(), request.description().trim(),
                technologies.stream().map(Technology::getName).reduce((left, right) -> left + ", " + right).orElse(""),
                normalizeOptional(request.repositoryUrl()), normalizeOptional(request.liveUrl()),
                normalizeOptional(request.imageUrl()), request.featured(), request.sortOrder());
        project.getTechnologyEntities().clear();
        project.getTechnologyEntities().addAll(technologies);
    }

    private List<Technology> resolveTechnologiesById(List<Long> technologyIds) {
        List<Long> ids = technologyIds == null ? List.of() : technologyIds;
        Set<Long> uniqueIds = new HashSet<>(ids);
        if (uniqueIds.size() != ids.size()) {
            throw new ConflictException("Technology IDs must not contain duplicates.");
        }
        List<Technology> technologies = new ArrayList<>(technologyRepository.findAllById(uniqueIds));
        if (technologies.size() != uniqueIds.size()) {
            throw new ResourceNotFoundException("Technology", "one or more supplied IDs");
        }
        return technologies;
    }

    private List<Technology> resolveTechnologiesByName(List<String> technologyNames) {
        List<Technology> resolved = new ArrayList<>();
        Set<String> uniqueNames = new HashSet<>();
        for (String rawName : technologyNames) {
            String name = rawName.trim();
            if (!uniqueNames.add(name.toLowerCase(Locale.ROOT))) {
                throw new ConflictException("Technology names must not contain duplicates.");
            }
            Technology technology = technologyRepository.findByNameIgnoreCase(name)
                    .orElseGet(() -> createTechnology(name));
            resolved.add(technology);
        }
        return resolved;
    }

    private Technology createTechnology(String name) {
        String baseSlug = name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
        if (baseSlug.isEmpty()) {
            baseSlug = "technology";
        }
        String slug = baseSlug;
        int suffix = 2;
        while (technologyRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + suffix++;
        }
        return technologyRepository.save(new Technology(name, slug));
    }

    private void saveChildren(Project project, ProjectWriteRequest request) {
        List<ProjectWriteRequest.ProjectLinkWriteRequest> links =
                request.links() == null ? List.of() : request.links();
        projectLinkRepository.saveAll(links.stream()
                .map(link -> new ProjectLink(project, link.label().trim(), link.url().trim(), link.linkType()))
                .toList());

        List<ProjectWriteRequest.ProjectImageWriteRequest> images =
                request.images() == null ? List.of() : request.images();
        projectImageRepository.saveAll(images.stream()
                .map(image -> new ProjectImage(project, image.imageUrl().trim(), normalizeOptional(image.altText()),
                        normalizeOptional(image.heading()), normalizeOptional(image.description()), image.sortOrder()))
                .toList());
    }

    private void writeAudit(PortfolioUserDetails actor, String action, Project project,
            Map<String, String> before, Map<String, String> after) {
        Map<String, Object> details = Map.of(
                "before", before == null ? Map.of() : before,
                "after", after == null ? Map.of() : after);
        auditLogRepository.save(new AuditLog(userRepository.getReferenceById(actor.getId()),
                action, "PROJECT", project.getId(), objectMapper.valueToTree(details)));
    }

    private Map<String, String> snapshot(Project project) {
        return Map.of("title", project.getTitle(), "slug", project.getSlug());
    }

    private ProjectResponse getProjectResponse(String slug) {
        return projectService.getProject(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Project", slug));
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
