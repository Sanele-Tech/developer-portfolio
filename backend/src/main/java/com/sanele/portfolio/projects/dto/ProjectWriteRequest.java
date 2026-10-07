package com.sanele.portfolio.projects.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.hibernate.validator.constraints.UniqueElements;

public record ProjectWriteRequest(
        @NotBlank @Size(max = 160) String title,
        @NotBlank @Size(max = 180) @Pattern(regexp = "[a-z0-9]+(?:-[a-z0-9]+)*") String slug,
        @NotBlank String description,
        @UniqueElements List<@NotNull @Positive Long> technologyIds,
        @Size(max = 30) List<@NotBlank @Size(max = 120) String> technologyNames,
        @Pattern(regexp = "^(https?://[^\\s/]+(?:/[^\\s]*)?)?$") @Size(max = 500) String repositoryUrl,
        @Pattern(regexp = "^(https?://[^\\s/]+(?:/[^\\s]*)?)?$") @Size(max = 500) String liveUrl,
        @Pattern(regexp = "^(https?://[^\\s/]+(?:/[^\\s]*)?)?$") @Size(max = 500) String imageUrl,
        boolean featured,
        @Min(0) @Max(100000) int sortOrder,
        List<@NotNull @Valid ProjectLinkWriteRequest> links,
        List<@NotNull @Valid ProjectImageWriteRequest> images) {

    public record ProjectLinkWriteRequest(
            @NotBlank @Size(max = 80) String label,
            @NotBlank @Pattern(regexp = "https?://[^\\s/]+(?:/[^\\s]*)?") @Size(max = 500) String url,
            @NotBlank @Pattern(regexp = "[A-Z_]{1,40}") String linkType) { }

    public record ProjectImageWriteRequest(
            @NotBlank @Pattern(regexp = "https?://[^\\s/]+(?:/[^\\s]*)?") @Size(max = 500) String imageUrl,
            @Size(max = 255) String altText,
            @Size(max = 160) String heading,
            String description,
            @Min(0) @Max(100000) int sortOrder) { }
}
