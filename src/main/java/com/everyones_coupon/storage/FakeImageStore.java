package com.everyones_coupon.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * Fake image store implementation for development/testing.
 * Discards image bytes and returns a plausible-looking URL with UUID.
 */
@Component
@Primary
public class FakeImageStore implements ImageStore {

    @Value("${app.image.fake-baseurl:/uploads}")
    private String baseUrl = "/uploads";

    @Override
    public String saveImage(byte[] imageBytes, String filename) throws IOException {
        // Do not persist anything; just generate a plausible URL
        if (filename == null || filename.isBlank()) {
            filename = UUID.randomUUID().toString() + ".jpg";
        } else {
            // Normalize only the filename portion (drop any path components)
            int idx = Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\'));
            if (idx >= 0 && idx < filename.length() - 1) {
                filename = filename.substring(idx + 1);
            }
        }
        // ensure extension exists
        if (!filename.contains(".")) {
            filename = filename + ".jpg";
        }
        // Make URL look plausible
        String uuidPart = UUID.randomUUID().toString();
        return String.format("%s/%s-%s", baseUrl.replaceAll("/+$", ""), uuidPart, filename);
    }
}
