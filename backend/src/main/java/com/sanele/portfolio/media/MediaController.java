package com.sanele.portfolio.media;

import com.sanele.portfolio.media.dto.MediaUploadResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class MediaController {
    private final MediaStorageService mediaStorageService;

    public MediaController(MediaStorageService mediaStorageService) {
        this.mediaStorageService = mediaStorageService;
    }

    @PostMapping(path = "/api/admin/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public MediaUploadResponse upload(@RequestParam("file") MultipartFile file,
            @RequestParam MediaPurpose purpose) {
        return mediaStorageService.store(file, purpose);
    }

    @GetMapping("/media/{filename}")
    public ResponseEntity<Resource> getMedia(@PathVariable String filename) {
        MediaStorageService.StoredMedia media = mediaStorageService.load(filename);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(media.mimeType()))
                .contentLength(media.sizeBytes())
                .cacheControl(CacheControl.maxAge(java.time.Duration.ofDays(365)).cachePublic().immutable())
                .header("X-Content-Type-Options", "nosniff")
                .body(media.resource());
    }
}
