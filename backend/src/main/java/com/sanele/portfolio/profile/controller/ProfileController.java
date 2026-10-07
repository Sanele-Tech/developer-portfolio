package com.sanele.portfolio.profile.controller;

import com.sanele.portfolio.profile.entity.Profile;
import com.sanele.portfolio.profile.repository.ProfileRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    private final ProfileRepository profileRepository;
    public ProfileController(ProfileRepository profileRepository) { this.profileRepository = profileRepository; }
    @GetMapping
    public ResponseEntity<Profile> getProfile() {
        return profileRepository.findFirstByOrderByIdAsc().map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}