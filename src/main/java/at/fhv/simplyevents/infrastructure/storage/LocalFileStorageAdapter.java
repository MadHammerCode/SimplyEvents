package at.fhv.simplyevents.infrastructure.storage;

import at.fhv.simplyevents.application.port.out.FileStoragePort;
import at.fhv.simplyevents.config.StorageProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class LocalFileStorageAdapter implements FileStoragePort {

    private final StorageProperties properties;
    private final Set<String> allowedExt;

    public LocalFileStorageAdapter(StorageProperties properties) {
        this.properties = properties;
        this.allowedExt = parseExtensions(properties.getAllowedExtensions());
    }

    @Override
    public String store(byte[] content, String filename) {
        if (content == null || content.length == 0) {
            throw new IllegalArgumentException("File content is empty.");
        }
        if (content.length > properties.getMaxSizeBytes()) {
            throw new IllegalArgumentException("File is too large. Max size is " + properties.getMaxSizeBytes() + " bytes.");
        }
        String extension = extractExtension(filename);
        if (!allowedExt.contains(extension)) {
            throw new IllegalArgumentException("Unsupported file type. Allowed: " + String.join(", ", allowedExt));
        }

        String storedFileName = buildStoredFileName(extension);
        Path baseDir = Paths.get(properties.getUploadDir());
        try {
            Files.createDirectories(baseDir);
            Path target = baseDir.resolve(storedFileName).normalize();
            Files.write(target, content);
            return baseDir.isAbsolute() ? target.toString() : baseDir.resolve(storedFileName).toString().replace("\\", "/");
        } catch (IOException e) {
            throw new IllegalStateException("Could not store file.", e);
        }
    }

    @Override
    public void delete(String path) {
        if (path == null || path.isBlank()) {
            return;
        }
        Path baseDir = Paths.get(properties.getUploadDir());
        Path target = Paths.get(path);
        if (!target.isAbsolute()) {
            String sanitized = stripLeadingBase(path, baseDir.getFileName().toString());
            target = baseDir.resolve(sanitized).normalize();
        }
        try {
            Files.deleteIfExists(target);
        } catch (IOException ignored) {
            // swallow deletion issues to avoid breaking business flow
        }
    }

    private Set<String> parseExtensions(String csv) {
        return Set.of(csv.split(","))
                .stream()
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(s -> s.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new IllegalArgumentException("Filename must contain an extension.");
        }
        String ext = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        if (ext.isBlank()) {
            throw new IllegalArgumentException("Filename must contain an extension.");
        }
        return ext;
    }

    private String buildStoredFileName(String extension) {
        return "event_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().replace("-", "") + "." + extension;
    }

    private String stripLeadingBase(String path, String baseName) {
        String normalized = path.startsWith("./") ? path.substring(2) : path;
        if (normalized.startsWith(baseName + "/")) {
            return normalized.substring(baseName.length() + 1);
        }
        return normalized;
    }
}
