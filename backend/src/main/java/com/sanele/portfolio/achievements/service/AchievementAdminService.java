package com.sanele.portfolio.achievements.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanele.portfolio.achievements.dto.AchievementResponse;
import com.sanele.portfolio.achievements.dto.AchievementWriteRequest;
import com.sanele.portfolio.achievements.entity.Achievement;
import com.sanele.portfolio.achievements.repository.AchievementRepository;
import com.sanele.portfolio.auth.security.PortfolioUserDetails;
import com.sanele.portfolio.audit.entity.AuditLog;
import com.sanele.portfolio.audit.repository.AuditLogRepository;
import com.sanele.portfolio.common.exception.ResourceNotFoundException;
import com.sanele.portfolio.profile.entity.Profile;
import com.sanele.portfolio.profile.repository.ProfileRepository;
import com.sanele.portfolio.users.repository.UserRepository;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AchievementAdminService {
    private final AchievementRepository achievementRepository;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public AchievementAdminService(AchievementRepository achievementRepository, ProfileRepository profileRepository,
            UserRepository userRepository, AuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.achievementRepository = achievementRepository;
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<AchievementResponse> getAchievements() {
        return profileRepository.findFirstByOrderByIdAsc()
                .map(profile -> achievementRepository.findByProfileIdOrderBySortOrderAscIdAsc(profile.getId())
                        .stream().map(this::toResponse).toList())
                .orElseGet(List::of);
    }

    @Transactional(readOnly = true)
    public AchievementResponse getAchievement(Long id) {
        Profile profile = getProfile();
        return toResponse(findAchievement(id, profile.getId()));
    }

    @Transactional
    public AchievementResponse createAchievement(AchievementWriteRequest request, PortfolioUserDetails actor) {
        Profile profile = getProfile();
        Achievement achievement = new Achievement(profile, request.title().trim(), normalizeCardType(request.cardType()),
                normalizeOptional(request.issuer()), normalizeOptional(request.description()),
                request.achievedOn(), normalizeOptional(request.url()),
                firstImage(request.imageUrls(), request.imageUrl()),
                normalizeImageUrls(request.imageUrls(), request.imageUrl()), request.sortOrder());
        achievementRepository.save(achievement);
        writeAudit(actor, "CREATE", achievement, null, snapshot(achievement));
        return toResponse(achievement);
    }

    @Transactional
    public AchievementResponse updateAchievement(Long id, AchievementWriteRequest request, PortfolioUserDetails actor) {
        Profile profile = getProfile();
        Achievement achievement = findAchievement(id, profile.getId());
        Map<String, Object> before = snapshot(achievement);
        achievement.updateDetails(profile, request.title().trim(), normalizeCardType(request.cardType()),
                normalizeOptional(request.issuer()),
                normalizeOptional(request.description()), request.achievedOn(),
                normalizeOptional(request.url()), firstImage(request.imageUrls(), request.imageUrl()),
                normalizeImageUrls(request.imageUrls(), request.imageUrl()), request.sortOrder());
        achievementRepository.save(achievement);
        writeAudit(actor, "UPDATE", achievement, before, snapshot(achievement));
        return toResponse(achievement);
    }

    @Transactional
    public void deleteAchievement(Long id, PortfolioUserDetails actor) {
        Profile profile = getProfile();
        Achievement achievement = findAchievement(id, profile.getId());
        writeAudit(actor, "DELETE", achievement, snapshot(achievement), null);
        achievementRepository.delete(achievement);
    }

    private Profile getProfile() {
        return profileRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "default"));
    }

    private Achievement findAchievement(Long id, Long profileId) {
        return achievementRepository.findByIdAndProfileId(id, profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Achievement", id.toString()));
    }

    private AchievementResponse toResponse(Achievement achievement) {
        return new AchievementResponse(achievement.getId(), achievement.getTitle(), achievement.getCardType(), achievement.getIssuer(),
                achievement.getDescription(), achievement.getAchievedOn(), achievement.getUrl(),
                achievement.getImageUrl(),
                achievement.getSortOrder(), achievement.getImageUrls());
    }

    private Map<String, Object> snapshot(Achievement achievement) {
        return Map.of(
                "title", achievement.getTitle(),
                "cardType", achievement.getCardType(),
                "issuer", achievement.getIssuer() == null ? "" : achievement.getIssuer(),
                "description", achievement.getDescription() == null ? "" : achievement.getDescription(),
                "achievedOn", achievement.getAchievedOn() == null ? "" : achievement.getAchievedOn().toString(),
                "url", achievement.getUrl() == null ? "" : achievement.getUrl(),
                "imageUrl", achievement.getImageUrl() == null ? "" : achievement.getImageUrl(),
                "imageUrls", achievement.getImageUrls(),
                "sortOrder", achievement.getSortOrder());
    }

    private String firstImage(List<String> imageUrls, String legacyImageUrl) {
        List<String> normalized = normalizeImageUrls(imageUrls, legacyImageUrl);
        return normalized.isEmpty() ? null : normalized.get(0);
    }

    private List<String> normalizeImageUrls(List<String> imageUrls, String legacyImageUrl) {
        List<String> source = imageUrls == null || imageUrls.isEmpty()
                ? (legacyImageUrl == null || legacyImageUrl.isBlank() ? List.of() : List.of(legacyImageUrl))
                : imageUrls;
        return source.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String normalizeCardType(String value) {
        return value == null || value.isBlank() ? "CERTIFICATION" : value.trim();
    }

    private void writeAudit(PortfolioUserDetails actor, String action, Achievement achievement,
            Map<String, Object> before, Map<String, Object> after) {
        Map<String, Object> details = Map.of(
                "before", before == null ? Map.of() : before,
                "after", after == null ? Map.of() : after);
        auditLogRepository.save(new AuditLog(userRepository.getReferenceById(actor.getId()),
                action, "ACHIEVEMENT", achievement.getId(), objectMapper.valueToTree(details)));
    }
}
