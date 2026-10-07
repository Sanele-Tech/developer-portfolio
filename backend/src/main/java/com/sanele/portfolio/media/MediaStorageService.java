package com.sanele.portfolio.media;

import com.sanele.portfolio.common.exception.ResourceNotFoundException;
import com.sanele.portfolio.media.exception.InvalidMediaException;
import com.sanele.portfolio.media.dto.MediaUploadResponse;
import com.sanele.portfolio.media.entity.MediaAsset;
import com.sanele.portfolio.media.repository.MediaAssetRepository;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MediaStorageService {
    private static final long MAX_SIZE_BYTES = 10 * 1024 * 1024;
    private final MediaAssetRepository mediaAssetRepository;
    private final String publicBaseUrl;

    public MediaStorageService(MediaAssetRepository mediaAssetRepository,
            @Value("${portfolio.media.public-base-url:http://localhost:8080}") String publicBaseUrl) {
        this.mediaAssetRepository = mediaAssetRepository;
        this.publicBaseUrl = publicBaseUrl.replaceAll("/+$", "");
    }

    public MediaUploadResponse store(MultipartFile file, MediaPurpose purpose) {
        if (file == null || file.isEmpty()) {
            throw new InvalidMediaException("Choose a file to upload.");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new InvalidMediaException("Files must be 10 MB or smaller.");
        }

        try {
            byte[] content = file.getBytes();
            DetectedMedia detected = detect(content);
            if (purpose == MediaPurpose.IMAGE && !detected.image()
                    || purpose == MediaPurpose.DOCUMENT && detected.image()) {
                throw new InvalidMediaException(purpose == MediaPurpose.IMAGE
                        ? "Upload a JPEG, PNG, WebP, or GIF image."
                        : "Upload a PDF document.");
            }
            String storageKey = UUID.randomUUID().toString().replace("-", "") + "." + detected.extension();
            mediaAssetRepository.save(new MediaAsset(storageKey, detected.mimeType(), content));
            return new MediaUploadResponse(
                    publicBaseUrl + "/media/" + storageKey, detected.mimeType(), content.length);
        } catch (java.io.IOException exception) {
            throw new IllegalStateException("The uploaded media could not be read.", exception);
        }
    }

    public StoredMedia load(String filename) {
        if (filename == null || !filename.matches("[a-f0-9]{32}\\.(?:jpg|png|webp|gif|pdf)")) {
            throw new ResourceNotFoundException("Media", String.valueOf(filename));
        }
        MediaAsset asset = mediaAssetRepository.findById(filename)
                .orElseThrow(() -> new ResourceNotFoundException("Media", filename));
        byte[] content = asset.getContent();
        Resource resource = new ByteArrayResource(content);
        return new StoredMedia(resource, asset.getMimeType(), asset.getSizeBytes());
    }

    private DetectedMedia detect(byte[] bytes) {
        if (startsWith(bytes, new byte[] {(byte) 0xff, (byte) 0xd8, (byte) 0xff})) {
            return new DetectedMedia("jpg", "image/jpeg", true);
        }
        if (startsWith(bytes, new byte[] {(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a})) {
            return new DetectedMedia("png", "image/png", true);
        }
        if (startsWithAscii(bytes, "GIF87a") || startsWithAscii(bytes, "GIF89a")) {
            return new DetectedMedia("gif", "image/gif", true);
        }
        if (startsWithAscii(bytes, "RIFF") && bytes.length >= 12
                && new String(bytes, 8, 4, java.nio.charset.StandardCharsets.US_ASCII).equals("WEBP")) {
            return new DetectedMedia("webp", "image/webp", true);
        }
        if (startsWithAscii(bytes, "%PDF-")) {
            return new DetectedMedia("pdf", "application/pdf", false);
        }
        throw new InvalidMediaException("The file contents are not a supported image or PDF.");
    }

    private boolean startsWith(byte[] bytes, byte[] signature) {
        if (bytes.length < signature.length) return false;
        for (int index = 0; index < signature.length; index++) {
            if (bytes[index] != signature[index]) return false;
        }
        return true;
    }

    private boolean startsWithAscii(byte[] bytes, String signature) {
        return bytes.length >= signature.length()
                && new String(bytes, 0, signature.length(), java.nio.charset.StandardCharsets.US_ASCII)
                        .toUpperCase(Locale.ROOT).equals(signature.toUpperCase(Locale.ROOT));
    }

    private record DetectedMedia(String extension, String mimeType, boolean image) { }

    public record StoredMedia(Resource resource, String mimeType, long sizeBytes) { }
}
