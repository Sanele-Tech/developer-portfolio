package com.sanele.portfolio.projects.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanele.portfolio.auth.security.PortfolioUserDetails;
import com.sanele.portfolio.audit.entity.AuditLog;
import com.sanele.portfolio.audit.repository.AuditLogRepository;
import com.sanele.portfolio.common.exception.ConflictException;
import com.sanele.portfolio.projects.dto.ProjectResponse;
import com.sanele.portfolio.projects.dto.ProjectWriteRequest;
import com.sanele.portfolio.projects.entity.Project;
import com.sanele.portfolio.projects.entity.Technology;
import com.sanele.portfolio.projects.repository.ProjectImageRepository;
import com.sanele.portfolio.projects.repository.ProjectLinkRepository;
import com.sanele.portfolio.projects.repository.ProjectRepository;
import com.sanele.portfolio.projects.repository.TechnologyRepository;
import com.sanele.portfolio.users.entity.User;
import com.sanele.portfolio.users.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProjectAdminServiceTest {
    private static final PortfolioUserDetails ACTOR =
            new PortfolioUserDetails(5L, "admin", "admin@example.test", "Admin", "", true, List.of());
    private static final ProjectResponse RESPONSE =
            new ProjectResponse(11L, "First project", "first-project", "Description",
                    List.of(), List.of(), null, null, null, false, 0, List.of(), List.of(), null, null);

    @Mock private ProjectRepository projectRepository;
    @Mock private ProjectLinkRepository projectLinkRepository;
    @Mock private ProjectImageRepository projectImageRepository;
    @Mock private TechnologyRepository technologyRepository;
    @Mock private UserRepository userRepository;
    @Mock private AuditLogRepository auditLogRepository;
    @Mock private ProjectService projectService;

    private ProjectAdminService service;

    @BeforeEach
    void setUp() {
        service = new ProjectAdminService(projectRepository, projectLinkRepository, projectImageRepository,
                technologyRepository, userRepository, auditLogRepository, projectService, new ObjectMapper());
    }

    @Test
    void createProjectCreatesAndAssociatesNewTechnologyNames() {
        ProjectWriteRequest request = new ProjectWriteRequest("First project", "first-project", "Description",
                List.of(), List.of("Java"), null, null, null, false, 0, List.of(), List.of());
        when(projectRepository.existsBySlug(request.slug())).thenReturn(false);
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(technologyRepository.findByNameIgnoreCase("Java")).thenReturn(Optional.empty());
        when(technologyRepository.existsBySlug("java")).thenReturn(false);
        when(technologyRepository.save(any(Technology.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.getReferenceById(ACTOR.getId())).thenReturn(mock(User.class));
        when(projectService.getProject(request.slug())).thenReturn(Optional.of(RESPONSE));

        service.createProject(request, ACTOR);

        ArgumentCaptor<Technology> technologyCaptor = ArgumentCaptor.forClass(Technology.class);
        verify(technologyRepository).save(technologyCaptor.capture());
        assertEquals("Java", technologyCaptor.getValue().getName());
        assertEquals("java", technologyCaptor.getValue().getSlug());
        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
        verify(projectRepository).save(projectCaptor.capture());
        assertEquals("Java", projectCaptor.getValue().getTechnologyEntities().iterator().next().getName());
    }

    @Test
    void createProjectSavesTheProjectAndAuditEntry() {
        ProjectWriteRequest request = request();
        when(projectRepository.existsBySlug(request.slug())).thenReturn(false);
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(technologyRepository.findAllById(anyCollection())).thenReturn(List.of());
        when(userRepository.getReferenceById(ACTOR.getId())).thenReturn(mock(User.class));
        when(projectService.getProject(request.slug())).thenReturn(Optional.of(RESPONSE));

        assertEquals(RESPONSE, service.createProject(request, ACTOR));

        ArgumentCaptor<AuditLog> auditCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditCaptor.capture());
        assertEquals("CREATE", auditCaptor.getValue().getAction());
        assertEquals("PROJECT", auditCaptor.getValue().getEntityType());
        assertTrue(auditCaptor.getValue().getDetails().get("after").get("slug").asText().equals("first-project"));
    }

    @Test
    void createProjectRejectsDuplicateSlugWithoutWritingAudit() {
        ProjectWriteRequest request = request();
        when(projectRepository.existsBySlug(request.slug())).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.createProject(request, ACTOR));

        verify(projectRepository, never()).save(any(Project.class));
        verify(auditLogRepository, never()).save(any(AuditLog.class));
    }

    @Test
    void updateProjectReplacesChildrenAndRecordsAuditEntry() {
        Project project = new Project();
        project.updateContent("Old title", "old-project", "Old description", "",
                null, null, null, false, 0);
        ProjectWriteRequest request = request();
        when(projectRepository.findById(11L)).thenReturn(Optional.of(project));
        when(projectRepository.existsBySlugAndIdNot(request.slug(), 11L)).thenReturn(false);
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(technologyRepository.findAllById(anyCollection())).thenReturn(List.of());
        when(userRepository.getReferenceById(ACTOR.getId())).thenReturn(mock(User.class));
        when(projectService.getProject(request.slug())).thenReturn(Optional.of(RESPONSE));

        assertEquals(RESPONSE, service.updateProject(11L, request, ACTOR));

        verify(projectLinkRepository).deleteByProjectId(11L);
        verify(projectImageRepository).deleteByProjectId(11L);
        ArgumentCaptor<AuditLog> auditCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditCaptor.capture());
        assertEquals("UPDATE", auditCaptor.getValue().getAction());
        assertTrue(auditCaptor.getValue().getDetails().get("before").get("slug").asText().equals("old-project"));
        assertTrue(auditCaptor.getValue().getDetails().get("after").get("slug").asText().equals("first-project"));
    }

    @Test
    void deleteProjectRecordsAuditBeforeRemovingTheProject() {
        Project project = new Project();
        project.updateContent("First project", "first-project", "Description", "",
                null, null, null, false, 0);
        when(projectRepository.findById(11L)).thenReturn(Optional.of(project));
        when(userRepository.getReferenceById(ACTOR.getId())).thenReturn(mock(User.class));

        service.deleteProject(11L, ACTOR);

        verify(auditLogRepository).save(any(AuditLog.class));
        verify(projectRepository).delete(project);
    }

    private ProjectWriteRequest request() {
        return new ProjectWriteRequest("First project", "first-project", "Description", List.of(), null,
                null, null, null, false, 0, List.of(), List.of());
    }
}
