package com.sanele.portfolio.achievements.service;

import com.sanele.portfolio.achievements.dto.AchievementResponse;
import com.sanele.portfolio.achievements.repository.AchievementRepository;
import com.sanele.portfolio.profile.repository.ProfileRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AchievementService {
    private final AchievementRepository achievementRepository;
    private final ProfileRepository profileRepository;
    public AchievementService(AchievementRepository achievementRepository, ProfileRepository profileRepository) {
        this.achievementRepository = achievementRepository;
        this.profileRepository = profileRepository;
    }
    public List<AchievementResponse> getAchievements() {
        return profileRepository.findFirstByOrderByIdAsc()
                .map(profile -> achievementRepository.findByProfileIdOrderBySortOrderAscIdAsc(profile.getId()).stream()
                        .map(item -> new AchievementResponse(item.getId(), item.getTitle(), item.getCardType(), item.getIssuer(),
                                item.getDescription(), item.getAchievedOn(), item.getUrl(), item.getImageUrl(),
                                item.getSortOrder(), item.getImageUrls()))
                        .toList())
                .orElseGet(List::of);
    }
}