package com.sanele.portfolio.profile.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sanele.portfolio.profile.dto.ProfileWriteRequest;
import com.sanele.portfolio.profile.entity.Profile;
import com.sanele.portfolio.profile.repository.ProfileRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ProfileAdminControllerTest {
    private final ProfileRepository profileRepository = mock(ProfileRepository.class);
    private final ProfileAdminController controller = new ProfileAdminController(profileRepository);

    @Test
    void createsProfileWhenNoRecordExists() {
        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());
        when(profileRepository.save(any(Profile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Profile result = controller.updateProfile(new ProfileWriteRequest(
                "Sanele Dlomo", "Developer", "Portfolio biography", "sanele@example.test",
                "+27 65 555 0199", null, "https://portfolio.test/media/avatar.png", null, null));

        assertEquals("Sanele Dlomo", result.getName());
        assertEquals("Developer", result.getTitle());
        assertEquals("Portfolio biography", result.getBio());
        assertEquals("sanele@example.test", result.getEmail());
        assertEquals("+27 65 555 0199", result.getPhoneNumber());
        assertEquals("https://portfolio.test/media/avatar.png", result.getAvatarUrl());
        verify(profileRepository).save(any(Profile.class));
    }
}
