package com.sanele.portfolio.admin.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.sanele.portfolio.achievements.repository.AchievementRepository;
import com.sanele.portfolio.content.entity.Page;
import com.sanele.portfolio.content.repository.PageRepository;
import com.sanele.portfolio.documents.repository.DocumentRepository;
import com.sanele.portfolio.education.repository.EducationRepository;
import com.sanele.portfolio.experience.repository.ExperienceRepository;
import com.sanele.portfolio.messages.entity.ContactMessage;
import com.sanele.portfolio.messages.repository.ContactMessageRepository;
import com.sanele.portfolio.projects.repository.ProjectRepository;
import com.sanele.portfolio.skills.repository.SkillRepository;
import org.junit.jupiter.api.Test;

class AdminOverviewServiceTest {
    private final ProjectRepository projectRepository = mock(ProjectRepository.class);
    private final ExperienceRepository experienceRepository = mock(ExperienceRepository.class);
    private final EducationRepository educationRepository = mock(EducationRepository.class);
    private final SkillRepository skillRepository = mock(SkillRepository.class);
    private final AchievementRepository achievementRepository = mock(AchievementRepository.class);
    private final DocumentRepository documentRepository = mock(DocumentRepository.class);
    private final PageRepository pageRepository = mock(PageRepository.class);
    private final ContactMessageRepository contactMessageRepository = mock(ContactMessageRepository.class);
    private final AdminOverviewService service = new AdminOverviewService(projectRepository, experienceRepository,
            educationRepository, skillRepository, achievementRepository, documentRepository, pageRepository,
            contactMessageRepository);

    @Test
    void returnsContentAndWorkflowCounts() {
        when(projectRepository.count()).thenReturn(3L);
        when(experienceRepository.count()).thenReturn(2L);
        when(educationRepository.count()).thenReturn(1L);
        when(skillRepository.count()).thenReturn(12L);
        when(achievementRepository.count()).thenReturn(4L);
        when(documentRepository.count()).thenReturn(2L);
        when(pageRepository.countByStatus(Page.Status.PUBLISHED)).thenReturn(5L);
        when(pageRepository.countByStatus(Page.Status.DRAFT)).thenReturn(2L);
        when(contactMessageRepository.countByStatus(ContactMessage.Status.NEW)).thenReturn(1L);

        var overview = service.getOverview();

        assertEquals(3L, overview.projects());
        assertEquals(12L, overview.skills());
        assertEquals(5L, overview.publishedPages());
        assertEquals(2L, overview.draftPages());
        assertEquals(1L, overview.newMessages());
    }
}
