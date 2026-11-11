package sn.guediawaye.backend.media;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

class MediaStorageServiceTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void storesValidImageWithGeneratedNameAndPublicUrl() throws IOException {
        byte[] imageBytes = createPng();
        MediaStorageService service = new MediaStorageService(
                temporaryDirectory.toString(), 10 * 1024 * 1024, "https://media.example.test/");

        MediaUploadResponse response = service.store(new MockMultipartFile(
                "file", "../../my-image.png", "text/plain", imageBytes));

        assertTrue(response.fileName().matches("[0-9a-f-]{36}\\.png"));
        assertEquals("https://media.example.test/media/" + response.fileName(), response.url());
        assertEquals("image/png", response.contentType());
        assertEquals(imageBytes.length, response.size());
        assertTrue(Files.exists(temporaryDirectory.resolve(response.fileName())));
    }

    @Test
    void rejectsNonImageContentEvenWhenFilenameLooksLikeAnImage() {
        MediaStorageService service = new MediaStorageService(temporaryDirectory.toString(), 1024, "http://localhost:8080");
        MockMultipartFile file = new MockMultipartFile("file", "image.png", "image/png", "not an image".getBytes());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.store(file));

        assertEquals(415, exception.getStatusCode().value());
        assertFalse(Files.exists(temporaryDirectory.resolve("image.png")));
    }

    @Test
    void rejectsFilesOverConfiguredLimit() {
        MediaStorageService service = new MediaStorageService(temporaryDirectory.toString(), 1, "http://localhost:8080");
        MockMultipartFile file = new MockMultipartFile("file", "image.png", "image/png", new byte[] {1, 2});

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.store(file));

        assertEquals(413, exception.getStatusCode().value());
    }

    private byte[] createPng() throws IOException {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        return output.toByteArray();
    }
}
