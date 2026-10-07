package com.sanele.portfolio.skills.service;

import com.sanele.portfolio.profile.repository.ProfileRepository;
import com.sanele.portfolio.skills.dto.SkillResponse;
import com.sanele.portfolio.skills.repository.SkillRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SkillService {
    private final SkillRepository skillRepository;
    private final ProfileRepository profileRepository;
    public SkillService(SkillRepository skillRepository, ProfileRepository profileRepository) {
        this.skillRepository = skillRepository;
        this.profileRepository = profileRepository;
    }
    public List<SkillResponse> getSkills() {
        return profileRepository.findFirstByOrderByIdAsc()
                .map(profile -> skillRepository.findByProfileIdOrderByCategoryAscSortOrderAsc(profile.getId()).stream()
                        .map(item -> new SkillResponse(item.getId(), item.getName(), item.getCategory(),
                                item.getProficiency(), item.getSortOrder()))
                        .toList())
                .orElseGet(List::of);
    }
}