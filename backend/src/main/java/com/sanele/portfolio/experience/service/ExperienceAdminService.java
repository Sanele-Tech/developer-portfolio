package com.sanele.portfolio.experience.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanele.portfolio.auth.security.PortfolioUserDetails;
import com.sanele.portfolio.audit.entity.AuditLog;
import com.sanele.portfolio.audit.repository.AuditLogRepository;
import com.sanele.portfolio.common.exception.ResourceNotFoundException;
import com.sanele.portfolio.experience.dto.ExperienceResponse;
import com.sanele.portfolio.experience.dto.ExperienceWriteRequest;
import com.sanele.portfolio.experience.entity.Experience;
import com.sanele.portfolio.experience.repository.ExperienceRepository;
import com.sanele.portfolio.profile.entity.Profile;
import com.sanele.portfolio.profile.repository.ProfileRepository;
import com.sanele.portfolio.users.repository.UserRepository;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExperienceAdminService {
    private final ExperienceRepository experienceRepository;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public ExperienceAdminService(ExperienceRepository experienceRepository, ProfileRepository profileRepository,
            UserRepository userRepository, AuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.experienceRepository = experienceRepository;
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<ExperienceResponse> getExperience() {
        return profileRepository.findFirstByOrderByIdAsc()
                .map(profile -> experienceRepository.findByProfileIdOrderBySortOrderAscIdAsc(profile.getId()).stream()
                        .map(this::toResponse)
                        .toList())
                .orElseGet(List::of);
    }

    @Transactional(readOnly = true)
    public ExperienceResponse getExperience(Long id) {
        Profile profile = getProfile();
        return toResponse(findExperience(id, profile.getId()));
    }

    @Transactional
    public ExperienceResponse createExperience(ExperienceWriteRequest request, PortfolioUserDetails actor) {
        Profile profile = getProfile();
        Experience experience = new Experience(profile, request.jobTitle().trim(), request.company().trim(),
                request.description().trim(), request.startDate(), request.currentRole() ? null : request.endDate(),
                request.currentRole(), request.sortOrder());
        experienceRepository.save(experience);
        writeAudit(actor, "CREATE", experience, null, snapshot(experience));
        return toResponse(experience);
    }

    @Transactional
    public ExperienceResponse updateExperience(Long id, ExperienceWriteRequest request, PortfolioUserDetails actor) {
        Profile profile = getProfile();
        Experience experience = findExperience(id, profile.getId());
        Map<String, Object> before = snapshot(experience);
        applyRequest(experience, profile, request);
        experienceRepository.save(experience);
        writeAudit(actor, "UPDATE", experience, before, snapshot(experience));
        return toResponse(experience);
    }

    @Transactional
    public void deleteExperience(Long id, PortfolioUserDetails actor) {
        Profile profile = getProfile();
        Experience experience = findExperience(id, profile.getId());
        writeAudit(actor, "DELETE", experience, snapshot(experience), null);
        experienceRepository.delete(experience);
    }

    private Profile getProfile() {
        return profileRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "default"));
    }

    private Experience findExperience(Long id, Long profileId) {
        return experienceRepository.findByIdAndProfileId(id, profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Experience", id.toString()));
    }

    private void applyRequest(Experience experience, Profile profile, ExperienceWriteRequest request) {
        experience.updateDetails(profile, request.jobTitle().trim(), request.company().trim(),
                request.description().trim(), request.startDate(), request.currentRole() ? null : request.endDate(),
                request.currentRole(), request.sortOrder());
    }

    private ExperienceResponse toResponse(Experience experience) {
        return new ExperienceResponse(experience.getId(), experience.getJobTitle(), experience.getCompany(),
                experience.getDescription(), experience.getStartDate(), experience.getEndDate(),
                experience.isCurrentRole(), experience.getSortOrder());
    }

    private Map<String, Object> snapshot(Experience experience) {
        return Map.of(
                "jobTitle", experience.getJobTitle(),
                "company", experience.getCompany(),
                "description", experience.getDescription(),
                "startDate", experience.getStartDate().toString(),
                "endDate", experience.getEndDate() == null ? "" : experience.getEndDate(),
                "currentRole", experience.isCurrentRole(),
                "sortOrder", experience.getSortOrder());
    }

    private void writeAudit(PortfolioUserDetails actor, String action, Experience experience,
            Map<String, Object> before, Map<String, Object> after) {
        Map<String, Object> details = Map.of(
                "before", before == null ? Map.of() : before,
                "after", after == null ? Map.of() : after);
        auditLogRepository.save(new AuditLog(userRepository.getReferenceById(actor.getId()),
                action, "EXPERIENCE", experience.getId(), objectMapper.valueToTree(details)));
    }
}
