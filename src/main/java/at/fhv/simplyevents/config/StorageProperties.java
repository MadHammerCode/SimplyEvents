package at.fhv.simplyevents.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "storage")
public class StorageProperties {
    /** Base directory for stored files. */
    private String uploadDir = "uploads";

    /** Max allowed file size in bytes. */
    private long maxSizeBytes = 5 * 1024 * 1024; // 5 MB

    /** Allowed file extensions, comma-separated. */
    private String allowedExtensions = "jpg,jpeg,png,gif,webp";

    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public long getMaxSizeBytes() {
        return maxSizeBytes;
    }

    public void setMaxSizeBytes(long maxSizeBytes) {
        this.maxSizeBytes = maxSizeBytes;
    }

    public String getAllowedExtensions() {
        return allowedExtensions;
    }

    public void setAllowedExtensions(String allowedExtensions) {
        this.allowedExtensions = allowedExtensions;
    }
}

