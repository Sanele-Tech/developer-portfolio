package com.sanele.portfolio.experience.service;

import com.sanele.portfolio.experience.dto.ExperienceResponse;
import com.sanele.portfolio.experience.repository.ExperienceRepository;
import com.sanele.portfolio.profile.repository.ProfileRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ExperienceService {
    private final ExperienceRepository experienceRepository;
    private final ProfileRepository profileRepository;
    public ExperienceService(ExperienceRepository experienceRepository, ProfileRepository profileRepository) {
        this.experienceRepository = experienceRepository;
        this.profileRepository = profileRepository;
    }
    public List<ExperienceResponse> getExperience() {
        return profileRepository.findFirstByOrderByIdAsc()
                .map(profile -> experienceRepository.findByProfileIdOrderBySortOrderAsc(profile.getId()).stream()
                        .map(item -> new ExperienceResponse(item.getId(), item.getJobTitle(), item.getCompany(),
                                item.getDescription(), item.getStartDate(), item.getEndDate(), item.isCurrentRole(), item.getSortOrder()))
                        .toList())
                .orElseGet(List::of);
    }
}