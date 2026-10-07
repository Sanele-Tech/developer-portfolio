package com.sanele.portfolio.skills.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanele.portfolio.auth.security.PortfolioUserDetails;
import com.sanele.portfolio.audit.entity.AuditLog;
import com.sanele.portfolio.audit.repository.AuditLogRepository;
import com.sanele.portfolio.common.exception.ResourceNotFoundException;
import com.sanele.portfolio.profile.entity.Profile;
import com.sanele.portfolio.profile.repository.ProfileRepository;
import com.sanele.portfolio.skills.dto.SkillResponse;
import com.sanele.portfolio.skills.dto.SkillWriteRequest;
import com.sanele.portfolio.skills.entity.Skill;
import com.sanele.portfolio.skills.repository.SkillRepository;
import com.sanele.portfolio.users.repository.UserRepository;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SkillAdminService {
    private final SkillRepository skillRepository;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public SkillAdminService(SkillRepository skillRepository, ProfileRepository profileRepository,
            UserRepository userRepository, AuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.skillRepository = skillRepository;
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<SkillResponse> getSkills() {
        return profileRepository.findFirstByOrderByIdAsc()
                .map(profile -> skillRepository.findByProfileIdOrderByCategoryAscSortOrderAscIdAsc(profile.getId())
                        .stream().map(this::toResponse).toList())
                .orElseGet(List::of);
    }

    @Transactional(readOnly = true)
    public SkillResponse getSkill(Long id) {
        Profile profile = getProfile();
        return toResponse(findSkill(id, profile.getId()));
    }

    @Transactional
    public SkillResponse createSkill(SkillWriteRequest request, PortfolioUserDetails actor) {
        Profile profile = getProfile();
        Skill skill = new Skill(profile, request.name().trim(), normalizeOptional(request.category()),
                proficiencyValue(request), request.sortOrder());
        skillRepository.save(skill);
        writeAudit(actor, "CREATE", skill, null, snapshot(skill));
        return toResponse(skill);
    }

    @Transactional
    public SkillResponse updateSkill(Long id, SkillWriteRequest request, PortfolioUserDetails actor) {
        Profile profile = getProfile();
        Skill skill = findSkill(id, profile.getId());
        Map<String, Object> before = snapshot(skill);
        skill.updateDetails(profile, request.name().trim(), normalizeOptional(request.category()),
                proficiencyValue(request), request.sortOrder());
        skillRepository.save(skill);
        writeAudit(actor, "UPDATE", skill, before, snapshot(skill));
        return toResponse(skill);
    }

    @Transactional
    public void deleteSkill(Long id, PortfolioUserDetails actor) {
        Profile profile = getProfile();
        Skill skill = findSkill(id, profile.getId());
        writeAudit(actor, "DELETE", skill, snapshot(skill), null);
        skillRepository.delete(skill);
    }

    private Profile getProfile() {
        return profileRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "default"));
    }

    private Skill findSkill(Long id, Long profileId) {
        return skillRepository.findByIdAndProfileId(id, profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill", id.toString()));
    }

    private SkillResponse toResponse(Skill skill) {
        return new SkillResponse(skill.getId(), skill.getName(), skill.getCategory(),
                skill.getProficiency(), skill.getSortOrder());
    }

    private Map<String, Object> snapshot(Skill skill) {
        return Map.of(
                "name", skill.getName(),
                "category", skill.getCategory() == null ? "" : skill.getCategory(),
                "proficiency", skill.getProficiency() == null ? "" : skill.getProficiency(),
                "sortOrder", skill.getSortOrder());
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private Short proficiencyValue(SkillWriteRequest request) {
        return request.proficiency() == null ? null : request.proficiency().shortValue();
    }

    private void writeAudit(PortfolioUserDetails actor, String action, Skill skill,
            Map<String, Object> before, Map<String, Object> after) {
        Map<String, Object> details = Map.of(
                "before", before == null ? Map.of() : before,
                "after", after == null ? Map.of() : after);
        auditLogRepository.save(new AuditLog(userRepository.getReferenceById(actor.getId()),
                action, "SKILL", skill.getId(), objectMapper.valueToTree(details)));
    }
}
