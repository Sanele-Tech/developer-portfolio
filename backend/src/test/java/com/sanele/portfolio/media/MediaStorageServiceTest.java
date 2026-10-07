package com.sanele.portfolio.media;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sanele.portfolio.media.exception.InvalidMediaException;
import com.sanele.portfolio.media.entity.MediaAsset;
import com.sanele.portfolio.media.repository.MediaAssetRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class MediaStorageServiceTest {
    private final MediaAssetRepository repository = mock(MediaAssetRepository.class);

    @Test
    void storesPdfBytesInDatabaseAndReturnsPublicUrl() {
        when(repository.save(any(MediaAsset.class))).thenAnswer(invocation -> invocation.getArgument(0));
        MediaStorageService service = new MediaStorageService(repository, "https://portfolio.test/");
        byte[] contents = "%PDF-1.7\nsample".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        MockMultipartFile upload = new MockMultipartFile("file", "resume.pdf",
                "application/pdf", contents);

        var response = service.store(upload, MediaPurpose.DOCUMENT);

        assertEquals("application/pdf", response.mimeType());
        assertEquals(contents.length, response.sizeBytes());
        assertTrue(response.url().startsWith("https://portfolio.test/media/"));
        verify(repository).save(any(MediaAsset.class));
    }

    @Test
    void rejectsNonPdfDocumentContent() {
        MediaStorageService service = new MediaStorageService(repository, "https://portfolio.test");
        MockMultipartFile upload = new MockMultipartFile("file", "resume.pdf",
                "application/pdf", "not a PDF".getBytes(java.nio.charset.StandardCharsets.US_ASCII));

        assertThrows(InvalidMediaException.class, () -> service.store(upload, MediaPurpose.DOCUMENT));
    }

    @Test
    void rejectsImageWhenDocumentWasRequested() {
        MediaStorageService service = new MediaStorageService(repository, "https://portfolio.test");
        byte[] pngSignature = {(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a};
        MockMultipartFile upload = new MockMultipartFile("file", "image.png", "image/png", pngSignature);

        assertThrows(InvalidMediaException.class, () -> service.store(upload, MediaPurpose.DOCUMENT));
    }

    @Test
    void retrievesStoredOriginalBytesAndMimeType() {
        byte[] contents = "%PDF-1.7\noriginal".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        String key = "0123456789abcdef0123456789abcdef.pdf";
        when(repository.findById(key)).thenReturn(Optional.of(new MediaAsset(key, "application/pdf", contents)));
        MediaStorageService service = new MediaStorageService(repository, "https://portfolio.test");

        var stored = service.load(key);

        assertEquals("application/pdf", stored.mimeType());
        assertEquals(contents.length, stored.sizeBytes());
        try {
            assertEquals(contents.length, stored.resource().getInputStream().readAllBytes().length);
        } catch (java.io.IOException exception) {
            throw new AssertionError("Stored media should be readable.", exception);
        }
    }
}
