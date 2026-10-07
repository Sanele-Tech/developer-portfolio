package com.sanele.portfolio.profile.controller;

import com.sanele.portfolio.profile.dto.ProfileWriteRequest;
import com.sanele.portfolio.profile.entity.Profile;
import com.sanele.portfolio.profile.repository.ProfileRepository;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/profile")
@PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
public class ProfileAdminController {
    private final ProfileRepository profileRepository;

    public ProfileAdminController(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @PutMapping
    @Transactional
    public Profile updateProfile(@Valid @RequestBody ProfileWriteRequest request) {
        Profile profile = profileRepository.findFirstByOrderByIdAsc().orElseGet(Profile::new);
        profile.updateDetails(request.name().trim(), request.title().trim(), request.bio().trim(),
                request.email().trim(), normalizeOptional(request.phoneNumber()), normalizeOptional(request.location()),
                normalizeOptional(request.avatarUrl()), normalizeOptional(request.githubUrl()),
                normalizeOptional(request.linkedinUrl()));
        return profileRepository.save(profile);
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
