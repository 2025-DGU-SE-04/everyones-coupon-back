package com.everyones_coupon.storage;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Component
public class LocalImageStore implements ImageStore {

    private final Path uploadsDir = Paths.get("uploads");

    public LocalImageStore() throws IOException {
        if (!Files.exists(uploadsDir)) Files.createDirectories(uploadsDir);
    }

    @Override
    public String saveImage(byte[] imageBytes, String filename) throws IOException {
        if (filename == null || filename.isBlank()) {
            filename = UUID.randomUUID().toString() + ".jpg";
        }
        Path out = uploadsDir.resolve(filename);
        Files.write(out, imageBytes);
        return "/uploads/" + filename;
    }
}
