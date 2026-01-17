package at.fhv.simplyevents.application.port.out;

public interface FileStoragePort {
    /**
     * Stores the given content under a generated target name derived from the original filename.
     *
     * @param content  file bytes to store
     * @param filename original filename (used to derive extension)
     * @return relative or absolute path to the stored file
     * @throws IllegalArgumentException if validation fails (e.g. size/extension)
     * @throws IllegalStateException    if the content cannot be written
     */
    String store(byte[] content, String filename);

    /**
     * Deletes a previously stored file if it exists.
     *
     * @param path path returned by {@link #store(byte[], String)}
     */
    void delete(String path);
}

