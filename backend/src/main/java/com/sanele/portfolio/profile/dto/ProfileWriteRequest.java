package com.sanele.portfolio.profile.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProfileWriteRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(max = 160) String title,
        @NotBlank @Size(max = 10000) String bio,
        @NotBlank @Email @Size(max = 320) String email,
        @Size(max = 40) String phoneNumber,
        @Size(max = 160) String location,
        @Pattern(regexp = "https?://[^\\s/]+(?:/[^\\s]*)?") @Size(max = 500) String avatarUrl,
        @Pattern(regexp = "https?://[^\\s/]+(?:/[^\\s]*)?") @Size(max = 500) String githubUrl,
        @Pattern(regexp = "https?://[^\\s/]+(?:/[^\\s]*)?") @Size(max = 500) String linkedinUrl) { }
