package com.sanele.portfolio.education.service;

import com.sanele.portfolio.education.dto.EducationResponse;
import com.sanele.portfolio.education.repository.EducationRepository;
import com.sanele.portfolio.profile.repository.ProfileRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EducationService {
    private final EducationRepository educationRepository;
    private final ProfileRepository profileRepository;
    public EducationService(EducationRepository educationRepository, ProfileRepository profileRepository) {
        this.educationRepository = educationRepository;
        this.profileRepository = profileRepository;
    }
    public List<EducationResponse> getEducation() {
        return profileRepository.findFirstByOrderByIdAsc()
                .map(profile -> educationRepository.findByProfileIdOrderBySortOrderAsc(profile.getId()).stream()
                        .map(item -> new EducationResponse(item.getId(), item.getInstitution(), item.getQualification(),
                                item.getDescription(), item.getStartDate(), item.getEndDate(), item.getSortOrder(),
                                item.getCoreModules(), item.getExperience(), item.getImageUrls()))
                        .toList())
                .orElseGet(List::of);
    }
}