package com.everyones_coupon.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class LocalImageStore implements ImageStore {

    private final Path uploadsDir;
    private final String baseUrl;
    public String getBaseUrl() { return baseUrl; }

    // Default constructor for compatibility (defaults to uploads and /uploads)
    public LocalImageStore() throws IOException {
        this("uploads", "/uploads");
    }

    public LocalImageStore(String uploadDir, String baseUrl) throws IOException {
        this.uploadsDir = Paths.get(uploadDir);
        this.baseUrl = (baseUrl == null || baseUrl.isBlank()) ? "/uploads" : baseUrl;
        if (!Files.exists(uploadsDir)) Files.createDirectories(uploadsDir);
    }

    @Override
    public String saveImage(byte[] imageBytes, String filename) throws IOException {
        if (filename == null || filename.isBlank()) {
            filename = UUID.randomUUID().toString() + ".jpg";
        }
        Path out = uploadsDir.resolve(filename);
        Files.write(out, imageBytes);
        String trimmed = baseUrl.replaceAll("/+$", "");
        return trimmed + "/" + filename;
    }
}
