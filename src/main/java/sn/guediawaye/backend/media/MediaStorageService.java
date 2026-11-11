package sn.guediawaye.backend.media;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MediaStorageService {

    private static final long MAX_IMAGE_PIXELS = 40_000_000L;
    private static final int MAX_IMAGE_DIMENSION = 10_000;
    private static final Set<String> ALLOWED_FORMATS = Set.of("jpeg", "jpg", "png", "gif");

    private final Path storageDirectory;
    private final long maxFileSize;
    private final String publicBaseUrl;

    public MediaStorageService(
            @Value("${app.media.storage-dir:./uploads}") String storageDirectory,
            @Value("${app.media.max-file-size:10485760}") long maxFileSize,
            @Value("${app.media.public-base-url:http://localhost:8080}") String publicBaseUrl) {
        this.storageDirectory = Path.of(storageDirectory).toAbsolutePath().normalize();
        this.maxFileSize = maxFileSize;
        this.publicBaseUrl = publicBaseUrl.replaceAll("/+$", "");
    }

    public MediaUploadResponse store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sélectionnez une image à envoyer.");
        }
        if (file.getSize() > maxFileSize) {
            throw new ResponseStatusException(HttpStatus.CONTENT_TOO_LARGE, "L’image dépasse la taille maximale autorisée de 10 Mo.");
        }

        ImageFormat imageFormat = inspectImage(file);
        String fileName = UUID.randomUUID() + "." + imageFormat.extension();
        Path target = storageDirectory.resolve(fileName).normalize();
        if (!target.startsWith(storageDirectory)) {
            throw new IllegalStateException("Le chemin de stockage de l’image est invalide.");
        }

        try {
            Files.createDirectories(storageDirectory);
            try (InputStream input = file.getInputStream()) {
                Files.copy(input, target);
            }
        } catch (IOException exception) {
            try {
                Files.deleteIfExists(target);
            } catch (IOException cleanupException) {
                exception.addSuppressed(cleanupException);
            }
            throw new IllegalStateException("Impossible d’enregistrer l’image sur le serveur.", exception);
        }

        return new MediaUploadResponse(
                publicBaseUrl + "/media/" + fileName,
                fileName,
                imageFormat.contentType(),
                file.getSize());
    }

    private ImageFormat inspectImage(MultipartFile file) {
        try (InputStream input = file.getInputStream();
                ImageInputStream imageInput = ImageIO.createImageInputStream(input)) {
            if (imageInput == null) {
                throw invalidImage();
            }
            var readers = ImageIO.getImageReaders(imageInput);
            if (!readers.hasNext()) {
                throw invalidImage();
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(imageInput, true, true);
                String format = reader.getFormatName().toLowerCase(Locale.ROOT);
                if (!ALLOWED_FORMATS.contains(format)) {
                    throw invalidImage();
                }
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width <= 0 || height <= 0
                        || width > MAX_IMAGE_DIMENSION
                        || height > MAX_IMAGE_DIMENSION
                        || (long) width * height > MAX_IMAGE_PIXELS) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Les dimensions de l’image dépassent les limites autorisées.");
                }
                return format.equals("jpg") || format.equals("jpeg")
                        ? new ImageFormat("jpg", "image/jpeg")
                        : new ImageFormat(format, "image/" + format);
            } finally {
                reader.dispose();
            }
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le fichier image est illisible.", exception);
        }
    }

    private ResponseStatusException invalidImage() {
        return new ResponseStatusException(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Format non pris en charge. Envoyez une image JPEG, PNG ou GIF valide.");
    }

    private record ImageFormat(String extension, String contentType) {
    }
}
