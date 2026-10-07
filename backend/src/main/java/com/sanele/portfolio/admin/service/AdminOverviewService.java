package com.sanele.portfolio.admin.service;

import com.sanele.portfolio.admin.dto.AdminOverviewResponse;
import com.sanele.portfolio.achievements.repository.AchievementRepository;
import com.sanele.portfolio.content.entity.Page.Status;
import com.sanele.portfolio.content.repository.PageRepository;
import com.sanele.portfolio.documents.repository.DocumentRepository;
import com.sanele.portfolio.education.repository.EducationRepository;
import com.sanele.portfolio.experience.repository.ExperienceRepository;
import com.sanele.portfolio.messages.entity.ContactMessage;
import com.sanele.portfolio.messages.repository.ContactMessageRepository;
import com.sanele.portfolio.projects.repository.ProjectRepository;
import com.sanele.portfolio.skills.repository.SkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminOverviewService {
    private final ProjectRepository projectRepository;
    private final ExperienceRepository experienceRepository;
    private final EducationRepository educationRepository;
    private final SkillRepository skillRepository;
    private final AchievementRepository achievementRepository;
    private final DocumentRepository documentRepository;
    private final PageRepository pageRepository;
    private final ContactMessageRepository contactMessageRepository;

    public AdminOverviewService(ProjectRepository projectRepository, ExperienceRepository experienceRepository,
            EducationRepository educationRepository, SkillRepository skillRepository,
            AchievementRepository achievementRepository, DocumentRepository documentRepository,
            PageRepository pageRepository, ContactMessageRepository contactMessageRepository) {
        this.projectRepository = projectRepository;
        this.experienceRepository = experienceRepository;
        this.educationRepository = educationRepository;
        this.skillRepository = skillRepository;
        this.achievementRepository = achievementRepository;
        this.documentRepository = documentRepository;
        this.pageRepository = pageRepository;
        this.contactMessageRepository = contactMessageRepository;
    }

    public AdminOverviewResponse getOverview() {
        return new AdminOverviewResponse(
                projectRepository.count(),
                experienceRepository.count(),
                educationRepository.count(),
                skillRepository.count(),
                achievementRepository.count(),
                documentRepository.count(),
                pageRepository.countByStatus(Status.PUBLISHED),
                pageRepository.countByStatus(Status.DRAFT),
                contactMessageRepository.countByStatus(ContactMessage.Status.NEW));
    }
}
