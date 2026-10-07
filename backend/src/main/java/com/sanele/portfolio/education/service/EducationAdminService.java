package com.sanele.portfolio.education.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanele.portfolio.auth.security.PortfolioUserDetails;
import com.sanele.portfolio.audit.entity.AuditLog;
import com.sanele.portfolio.audit.repository.AuditLogRepository;
import com.sanele.portfolio.common.exception.ResourceNotFoundException;
import com.sanele.portfolio.education.dto.EducationResponse;
import com.sanele.portfolio.education.dto.EducationWriteRequest;
import com.sanele.portfolio.education.entity.Education;
import com.sanele.portfolio.education.repository.EducationRepository;
import com.sanele.portfolio.profile.entity.Profile;
import com.sanele.portfolio.profile.repository.ProfileRepository;
import com.sanele.portfolio.users.repository.UserRepository;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EducationAdminService {
    private final EducationRepository educationRepository;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public EducationAdminService(EducationRepository educationRepository, ProfileRepository profileRepository,
            UserRepository userRepository, AuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.educationRepository = educationRepository;
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<EducationResponse> getEducation() {
        return profileRepository.findFirstByOrderByIdAsc()
                .map(profile -> educationRepository.findByProfileIdOrderBySortOrderAscIdAsc(profile.getId()).stream()
                        .map(this::toResponse)
                        .toList())
                .orElseGet(List::of);
    }

    @Transactional(readOnly = true)
    public EducationResponse getEducation(Long id) {
        Profile profile = getProfile();
        return toResponse(findEducation(id, profile.getId()));
    }

    @Transactional
    public EducationResponse createEducation(EducationWriteRequest request, PortfolioUserDetails actor) {
        Profile profile = getProfile();
        Education education = new Education(profile, request.institution().trim(), request.qualification().trim(),
                normalizeOptional(request.description()), request.startDate(), request.endDate(),
                normalizeValues(request.coreModules()), normalizeOptional(request.experience()),
                normalizeValues(request.imageUrls()), request.sortOrder());
        educationRepository.save(education);
        writeAudit(actor, "CREATE", education, null, snapshot(education));
        return toResponse(education);
    }

    @Transactional
    public EducationResponse updateEducation(Long id, EducationWriteRequest request, PortfolioUserDetails actor) {
        Profile profile = getProfile();
        Education education = findEducation(id, profile.getId());
        Map<String, Object> before = snapshot(education);
        education.updateDetails(profile, request.institution().trim(), request.qualification().trim(),
                normalizeOptional(request.description()), request.startDate(), request.endDate(),
                normalizeValues(request.coreModules()), normalizeOptional(request.experience()),
                normalizeValues(request.imageUrls()), request.sortOrder());
        educationRepository.save(education);
        writeAudit(actor, "UPDATE", education, before, snapshot(education));
        return toResponse(education);
    }

    @Transactional
    public void deleteEducation(Long id, PortfolioUserDetails actor) {
        Profile profile = getProfile();
        Education education = findEducation(id, profile.getId());
        writeAudit(actor, "DELETE", education, snapshot(education), null);
        educationRepository.delete(education);
    }

    private Profile getProfile() {
        return profileRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "default"));
    }

    private Education findEducation(Long id, Long profileId) {
        return educationRepository.findByIdAndProfileId(id, profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Education", id.toString()));
    }

    private EducationResponse toResponse(Education education) {
        return new EducationResponse(education.getId(), education.getInstitution(), education.getQualification(),
                education.getDescription(), education.getStartDate(), education.getEndDate(), education.getSortOrder(),
                education.getCoreModules(), education.getExperience(), education.getImageUrls());
    }

    private Map<String, Object> snapshot(Education education) {
        return Map.of(
                "institution", education.getInstitution(),
                "qualification", education.getQualification(),
                "description", education.getDescription() == null ? "" : education.getDescription(),
                "startDate", education.getStartDate() == null ? "" : education.getStartDate().toString(),
                "endDate", education.getEndDate() == null ? "" : education.getEndDate().toString(),
                "coreModules", education.getCoreModules(),
                "experience", education.getExperience() == null ? "" : education.getExperience(),
                "imageUrls", education.getImageUrls(),
                "sortOrder", education.getSortOrder());
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private List<String> normalizeValues(List<String> values) {
        return values == null ? List.of() : values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
    }

    private void writeAudit(PortfolioUserDetails actor, String action, Education education,
            Map<String, Object> before, Map<String, Object> after) {
        Map<String, Object> details = Map.of(
                "before", before == null ? Map.of() : before,
                "after", after == null ? Map.of() : after);
        auditLogRepository.save(new AuditLog(userRepository.getReferenceById(actor.getId()),
                action, "EDUCATION", education.getId(), objectMapper.valueToTree(details)));
    }
}
